package com.mocicarazvan.fileservice.service.impl;

import com.mocicarazvan.fileservice.dtos.FileUploadResponse;
import com.mocicarazvan.fileservice.dtos.GridIdsDto;
import com.mocicarazvan.fileservice.dtos.MetadataDto;
import com.mocicarazvan.fileservice.dtos.ToBeDeletedCounts;
import com.mocicarazvan.fileservice.enums.CustomMediaType;
import com.mocicarazvan.fileservice.enums.FileType;
import com.mocicarazvan.fileservice.exceptions.FileNotFound;
import com.mocicarazvan.fileservice.exceptions.NoFilesUploadedException;
import com.mocicarazvan.fileservice.models.Media;
import com.mocicarazvan.fileservice.models.MediaMetadata;
import com.mocicarazvan.fileservice.repositories.ExtendedMediaRepository;
import com.mocicarazvan.fileservice.repositories.MediaMetadataRepository;
import com.mocicarazvan.fileservice.repositories.MediaRepository;
import com.mocicarazvan.fileservice.service.BytesService;
import com.mocicarazvan.fileservice.service.MediaService;
import com.mocicarazvan.fileservice.utils.CacheHeaderUtils;
import com.mocicarazvan.fileservice.websocket.ProgressWebSocketHandler;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {
    protected final ReactiveGridFsTemplate gridFsTemplate;
    protected final MediaRepository mediaRepository;
    protected final MediaMetadataRepository mediaMetadataRepository;
    protected final ProgressWebSocketHandler progressWebSocketHandler;
    protected final BytesService bytesService;
    protected final ExtendedMediaRepository extendedMediaRepository;

    @Value("${images.url}")
    protected String imagesUrl;

    @Value("${videos.url}")
    protected String videosUrl;

    @Value("${hard-delete.batch-size:25}")
    protected Integer batchSize;

    @Override
    public Mono<FileUploadResponse> uploadFiles(Flux<FilePart> files, MetadataDto metadataDto) {
        String finalClientId = metadataDto.getClientId() != null ? metadataDto.getClientId() : "default";
        return files.index()
                .switchIfEmpty(Flux.error(new NoFilesUploadedException()))
//                .subscribeOn(Schedulers.parallel())
                .flatMap(indexedFilePart -> saveFileWithIndex(indexedFilePart.getT1(), indexedFilePart.getT2(), metadataDto)
                                .doOnNext(tuple -> {
//                            log.error("Sending progress update " + tuple.getT1());
                                    progressWebSocketHandler.sendProgressUpdate(finalClientId, metadataDto.getFileType(), tuple.getT1());
                                })
                )
                .collect(
                        ArrayList<String>::new,
                        (list, tuple) -> {
                            int idx = tuple.getT1().intValue();
                            if (list.size() <= idx) {
                                // ensure the list is large enough
                                while (list.size() <= idx) {
                                    list.add("");
                                }
                            }
                            list.set(idx, tuple.getT2());
                        }
                )
                .map(urls -> FileUploadResponse.builder()
                        .files(urls)
                        .fileType(metadataDto.getFileType())
                        .build())
                .doOnNext(_ ->
                        progressWebSocketHandler.sendCompletionMessage(
                                finalClientId,
                                metadataDto.getFileType()
                        )
                );

    }

    @Override
    public Mono<ServerHttpResponse> getResponseForFile(String gridId, Integer width, Integer height, Double quality, ServerWebExchange exchange) {
        return getFile(gridId)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(file -> file.getGridFSFile()
                        .flatMap(gridFSFile -> {


                                    FileType fileType = FileType.valueOf(file.getOptions().getMetadata().getString("fileType"));
                                    ServerHttpRequest request = exchange.getRequest();
                                    ServerHttpResponse response = exchange.getResponse();

                                    Mono<ServerHttpResponse> responseMono =
                                            fileType == FileType.VIDEO ?
                                                    fetchAndProcessVideo(request, response, file, gridFSFile, gridId) :
                                                    fetchAndProcessImage(request, response, file, gridFSFile, gridId, width, height, quality);

                                    return responseMono
                                            .onErrorResume(e -> {
                                                if (!response.isCommitted()) {
                                                    log.error("Error writing response", e);
                                                    CacheHeaderUtils.clearCacheHeaders(exchange.getResponse());
                                                    response.setStatusCode(HttpStatus.FOUND);
                                                    response.getHeaders().setLocation(URI.create("/files/download/" + gridId));
                                                }
                                                return response.setComplete()
                                                        .thenReturn(response);
                                            });
                                }
                        ))
                .doOnError(e -> {
                    log.error("Error getting file", e);
                    CacheHeaderUtils.clearCacheHeaders(exchange.getResponse());
                });

    }

    protected Mono<ServerHttpResponse> fetchAndProcessVideo(ServerHttpRequest request, ServerHttpResponse response,
                                                            ReactiveGridFsResource file, GridFSFile gridFSFile
            , String gridId) {
        List<HttpRange> httpRanges = request.getHeaders().getRange();
        String etag = CacheHeaderUtils.buildETag(gridId, gridFSFile.getUploadDate().getTime());
        String clientETag = request.getHeaders().getFirst(HttpHeaders.IF_NONE_MATCH);
        if (httpRanges.isEmpty() && CacheHeaderUtils.etagEquals(etag, clientETag)) {
            response.setStatusCode(HttpStatus.NOT_MODIFIED);
            return Mono.just(response);
        }

        long ifModifiedSince = request.getHeaders().getIfModifiedSince();
        if (httpRanges.isEmpty() && ifModifiedSince > 0 && gridFSFile.getUploadDate().getTime() <= ifModifiedSince) {
            response.setStatusCode(HttpStatus.NOT_MODIFIED);
            return Mono.just(response);
        }

        response.getHeaders().setETag(etag);
        CacheHeaderUtils.setCachingHeaders(response, gridFSFile, FileType.VIDEO, httpRanges);
        response.getHeaders().setContentDisposition(
                ContentDisposition.inline()
                        .filename(gridId + ".mp4")
                        .build()
        );
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "video/mp4");
        response.getHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");

        Flux<DataBuffer> downloadStream = file.getDownloadStream();
        if (!httpRanges.isEmpty()) {
            downloadStream = makeVideoRangeDownloadStream(file, httpRanges, gridFSFile.getLength(), response);
        } else {
            response.getHeaders().setContentLength(gridFSFile.getLength());
        }
        return response.writeWith(downloadStream.doOnDiscard(DataBuffer.class, DataBufferUtils::release))
                .thenReturn(response);

    }

    protected Mono<ServerHttpResponse> fetchAndProcessImage(ServerHttpRequest request, ServerHttpResponse response,
                                                            ReactiveGridFsResource file, GridFSFile gridFSFile
            , String gridId, Integer width, Integer height, Double quality
    ) {
        return isWebpOutputEnabled(request).flatMap(webpOutputEnabled -> {

            String etag = CacheHeaderUtils.buildETag(gridId, width, height, quality, webpOutputEnabled, gridFSFile.getUploadDate().getTime());
            String clientETag = request.getHeaders().getFirst(HttpHeaders.IF_NONE_MATCH);
            if (CacheHeaderUtils.etagEquals(etag, clientETag)) {
                response.setStatusCode(HttpStatus.NOT_MODIFIED);
                return Mono.just(response);
            }

            long ifModifiedSince = request.getHeaders().getIfModifiedSince();
            if (ifModifiedSince > 0 && gridFSFile.getUploadDate().getTime() <= ifModifiedSince) {
                response.setStatusCode(HttpStatus.NOT_MODIFIED);
                return Mono.just(response);
            }

            response.getHeaders().setETag(etag);
            CacheHeaderUtils.setCachingHeaders(response, gridFSFile, FileType.IMAGE);

            CustomMediaType customMediaType = CustomMediaType.fromValue(file.getOptions().getMetadata().getString("mediaType"));
            String fileAttch = !customMediaType.equals(CustomMediaType.ALL) ? "." + customMediaType.getValue() : "";

            if (webpOutputEnabled) {
                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "image/webp");
                response.getHeaders().setContentDisposition(
                        ContentDisposition.inline()
                                .filename(gridId + ".webp")
                                .build()
                );
            } else {
                response.getHeaders().setContentDisposition(
                        ContentDisposition.inline()
                                .filename(gridId + fileAttch)
                                .build()
                );
                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "image/" + customMediaType.getContentTypeValueMedia());
            }
            Flux<DataBuffer> downloadStream = file.getDownloadStream();

            if (width != null || height != null || quality != null) {
                downloadStream =
                        makeImageDownloadStream(gridId, width, height, quality, webpOutputEnabled, downloadStream, customMediaType, response, fileAttch);
            } else {
                response.getHeaders().setContentLength(gridFSFile.getLength());
            }
            return response.writeWith(downloadStream.doOnDiscard(DataBuffer.class, DataBufferUtils::release))
                    .thenReturn(response);
        });
    }

    protected Flux<DataBuffer> makeVideoRangeDownloadStream(ReactiveGridFsResource file, List<HttpRange> httpRanges, long fileLength, ServerHttpResponse response) {
        Flux<DataBuffer> downloadStream;
        HttpRange range = httpRanges.getFirst();
        long start = range.getRangeStart(fileLength);
        long end = range.getRangeEnd(fileLength);
        response.getHeaders().add(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, end, fileLength));
        response.getHeaders().setContentLength(end - start + 1);
        response.getHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");
        downloadStream = bytesService.getVideoByRange(file, start, end)
                .doOnNext(_ -> {
                    response.setStatusCode(HttpStatus.PARTIAL_CONTENT);
                });
        return downloadStream;
    }

    protected Flux<DataBuffer> makeImageDownloadStream(String gridId, Integer width, Integer height, Double quality, Boolean webpOutputEnabled, Flux<DataBuffer> downloadStream, CustomMediaType customMediaType, ServerHttpResponse response, String fileAttch) {
        return bytesService.convertWithThumblinator(width, height, quality, downloadStream, customMediaType, webpOutputEnabled, response
                )
                .flatMap(dataBuffer -> {
                    response.getHeaders().setContentLength(dataBuffer.readableByteCount());
                    return Mono.just(dataBuffer);
                });
    }


    protected Mono<Tuple2<Long, String>> saveFileWithIndex(Long index, FilePart filePart, MetadataDto metadataDto) {
        return saveFile(filePart, metadataDto)
                .map(media -> Tuples.of(index, generateFileUrl(media.getGridFsId(), metadataDto.getFileType())));
    }

    @Override
    public Mono<ReactiveGridFsResource> getFile(String gridId) {
        return getFileByGridId(gridId)
                .flatMap(gridFsTemplate::getResource);
    }

    protected Mono<GridFSFile> getFileByGridId(String gridId) {
        return gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(gridId))))
                .switchIfEmpty(Mono.error(new FileNotFound(gridId)));
    }

    @Override
    public Mono<GridFSFile> getFileInfo(String gridId) {
        return getFileByGridId(gridId);
    }

    @Override
    public Mono<Void> deleteFile(String gridId) {
        return gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(gridId))))
//                .subscribeOn(Schedulers.parallel())
                .then(Mono.defer(() -> mediaRepository.findAllByGridFsId(gridId)
                        .flatMap(media -> mediaMetadataRepository.deleteAllByMediaId(media.getId()))
                        .then(mediaRepository.deleteAllByGridFsId(gridId))));
    }

    @Override
    public Mono<Void> deleteFiles(List<String> gridIds) {


        return Flux.fromIterable(gridIds)
                .filter(ObjectId::isValid)
                .map(ObjectId::new)
                .collectList()
                .flatMap(objectIds -> {
                    if (objectIds.isEmpty()) {
                        log.warn("No valid grid IDs provided for deletion");
                        return Mono.empty();
                    }
                    if (objectIds.size() != gridIds.size()) {
                        log.error("Invalid grid ids: {}", gridIds);
                    }
                    return Mono.zip(
                                    gridFsTemplate.delete(new Query(Criteria.where("_id").in(objectIds)))
                                            .thenReturn(true),
                                    mediaRepository.findAllByGridFsIdIn(gridIds)
                                            .map(Media::getId)
                                            .collectList()
                                            .flatMap(mediaIds -> Mono.zip(
                                                    mediaMetadataRepository.deleteAllByMediaIdIn(mediaIds).thenReturn(true),
                                                    mediaRepository.deleteAllByGridFsIdIn(gridIds).thenReturn(true)
                                            ))
//                                .flatMap(mediaMetadataRepository::deleteAllByMediaIdIn)
//                                .then(mediaRepository.deleteAllByGridFsIdIn(gridIds))
                                            .thenReturn(true)
                            )
                            .doOnSuccess(tuple -> {
                                if (tuple.getT1() && tuple.getT2()) {
                                    log.info("Files deleted successfully");
                                } else {
                                    log.error("Error deleting files");
                                }
                            })
                            .then();
                });


    }

    @Override
    public Mono<ToBeDeletedCounts> countToBeDeleted() {
        return extendedMediaRepository.countAllByToBeDeletedIsTrue();
    }

    @Override
    public Mono<Void> markFilesToBeDeleted(GridIdsDto ids) {
        return extendedMediaRepository.markToBeDeletedByGridFsIds(ids.getGridFsIds()).then();
    }

    @Override
    public Flux<ToBeDeletedCounts> hardDeleteFiles() {
        return mediaRepository.findAllByToBeDeletedIsTrue()
                .map(Media::getGridFsId)
                .buffer(batchSize)
                .flatMapSequential(batch -> deleteFiles(batch)
                                .then(countToBeDeleted())
                        , 2);
    }

    protected String generateFileUrl(String id, FileType fileType) {
        return String.format("%s/download/%s",
                fileType.equals(FileType.IMAGE) ? imagesUrl : videosUrl
                , id);
    }

    protected Mono<Media> saveFile(FilePart filePart, MetadataDto metadataDto) {
        String mediaType = CustomMediaType.fromFileName(filePart.filename()).getValue();
        Document metadata = new Document("name", metadataDto.getName())
                .append("fileType", metadataDto.getFileType().name())
                .append("mediaType", mediaType);

        return gridFsTemplate.store(filePart.content(), filePart.filename(), Objects.requireNonNull(filePart.headers().getContentType()).toString(), metadata)
                .flatMap(gridFSId -> {
                    Media media = Media.builder()
                            .fileSize(filePart.headers().getContentLength())
                            .fileName(filePart.filename())
                            .fileType(metadataDto.getFileType().name())
                            .gridFsId(gridFSId.toHexString())
                            .mediaType(mediaType)
                            .build();
                    return mediaRepository.save(media)
                            .flatMap(m -> mediaMetadataRepository.save(MediaMetadata.builder()
                                            .name(filePart.filename())
                                            .mediaId(m.getId())
                                            .build())
                                    .thenReturn(media));
                });
    }


    protected Mono<Boolean> isWebpOutputEnabled(ServerHttpRequest request) {
        boolean webpOutputEnabledQueryParam = Boolean.parseBoolean(request.getQueryParams().getFirst("webpOutputEnabled"));
        if (webpOutputEnabledQueryParam) {
            return Mono.just(true);
        }
        return Flux.fromIterable(request.getHeaders().getAccept())
                .any(mediaType -> mediaType.isCompatibleWith(MediaType.valueOf("image/webp")));
    }

}

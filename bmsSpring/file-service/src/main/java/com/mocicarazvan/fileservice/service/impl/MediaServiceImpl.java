package com.mocicarazvan.fileservice.service.impl;

import com.mocicarazvan.fileservice.dtos.FileUploadResponse;
import com.mocicarazvan.fileservice.dtos.GridIdsDto;
import com.mocicarazvan.fileservice.dtos.MetadataDto;
import com.mocicarazvan.fileservice.enums.FileType;
import com.mocicarazvan.fileservice.enums.MediaType;
import com.mocicarazvan.fileservice.exceptions.FileNotFound;
import com.mocicarazvan.fileservice.models.Media;
import com.mocicarazvan.fileservice.models.MediaMetadata;
import com.mocicarazvan.fileservice.repositories.ImageRedisRepository;
import com.mocicarazvan.fileservice.repositories.MediaMetadataRepository;
import com.mocicarazvan.fileservice.repositories.MediaRepository;
import com.mocicarazvan.fileservice.service.BytesService;
import com.mocicarazvan.fileservice.service.MediaService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {
    private final ReactiveGridFsTemplate gridFsTemplate;
    private final MediaRepository mediaRepository;
    private final MediaMetadataRepository mediaMetadataRepository;
    private final ProgressWebSocketHandler progressWebSocketHandler;
    private final BytesService bytesService;
    private final ImageRedisRepository imageRedisRepository;
    @Value("${images.url}")
    private String imagesUrl;

    @Value("${videos.url}")
    private String videosUrl;

    @Override
    public Mono<FileUploadResponse> uploadFiles(Flux<FilePart> files, MetadataDto metadataDto) {
        return files.index()
//                .subscribeOn(Schedulers.parallel())
                .flatMap(indexedFilePart -> saveFileWithIndex(indexedFilePart.getT1(), indexedFilePart.getT2(), metadataDto)
                        .doOnNext(tuple -> {
                            log.error("Sending progress update " + tuple.getT1());
                            progressWebSocketHandler.sendProgressUpdate(metadataDto.getClientId() != null ? metadataDto.getClientId() : "default", metadataDto.getFileType(), tuple.getT1());
                        })
                )
                .collectList()
                .map(urls -> {
                    urls.sort(Comparator.comparing(Tuple2::getT1));
                    return FileUploadResponse.builder()
                            .files(urls.stream().map(Tuple2::getT2).toList())
                            .fileType(metadataDto.getFileType())
                            .build();
                })
                .doOnNext(_ ->
                        progressWebSocketHandler.sendCompletionMessage(
                                metadataDto.getClientId() != null ? metadataDto.getClientId() : "default",
                                metadataDto.getFileType()
                        )
                );

    }

    @Override
    public Mono<ServerHttpResponse> getResponseForFile(String gridId, Integer width, Integer height, Double quality, ServerWebExchange exchange, boolean shouldCheckCache) {
        return shouldCheckCache ?
                imageRedisRepository.getImage(gridId, width, height, quality).flatMap(
                                cachedImage -> {
//                                    log.info("Image found in cache");
                                    ServerHttpResponse response = exchange.getResponse();
                                    response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + gridId + "\"");
                                    response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "image/**");
                                    response.getHeaders().setContentLength(cachedImage.length);
                                    return response.writeWith(Mono.just(response.bufferFactory().wrap(cachedImage)))
                                            .thenReturn(response)
                                            .onErrorResume(e -> {
                                                response.setStatusCode(HttpStatus.FOUND);
                                                response.getHeaders().setLocation(URI.create("/files/download/" + gridId));
                                                return response.setComplete()
                                                        .thenReturn(response);
                                            });
                                }
                        )
                        .switchIfEmpty(fetchFileAndProcessFromGridFS(gridId, width, height, quality, exchange)) :
                fetchFileAndProcessFromGridFS(gridId, width, height, quality, exchange);
    }


    public Mono<ServerHttpResponse> fetchFileAndProcessFromGridFS(String gridId, Integer width, Integer height, Double quality, ServerWebExchange exchange) {
        return getFile(gridId)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(file -> file.getGridFSFile()
                        .flatMap(gridFSFile -> {
                            FileType fileType = FileType.valueOf(file.getOptions().getMetadata().getString("fileType"));
                            MediaType mediaType = MediaType.fromValue(file.getOptions().getMetadata().getString("mediaType"));
                            log.info("File type: {}", fileType);
                            ServerHttpRequest request = exchange.getRequest();
                            ServerHttpResponse response = exchange.getResponse();

                            response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"");
                            response.getHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");

                            if (fileType.equals(FileType.VIDEO)) {
                                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "video/mp4");
                            } else if (fileType.equals(FileType.IMAGE)) {
                                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "image/" + mediaType.getValue());
                            }


                            List<HttpRange> httpRanges = request.getHeaders().getRange();
                            Flux<DataBuffer> downloadStream = file.getDownloadStream();
                            long fileLength = gridFSFile.getLength();
//                            log.info("Range: " + httpRanges);

                            if (fileType.equals(FileType.IMAGE) && (width != null || height != null || quality != null)) {
//                                log.info("file name: {}", file.getFilename());
                                downloadStream =
                                        bytesService.convertWithThumblinator(width, height, quality, downloadStream, response
                                                )
                                                .flatMap(dataBuffer -> {
                                                    response.getHeaders().setContentLength(dataBuffer.readableByteCount());
                                                    ByteBuffer byteBuffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
                                                    dataBuffer.toByteBuffer(byteBuffer);
                                                    return imageRedisRepository.saveImage(gridId, width, height, quality, byteBuffer.array())
                                                            .thenReturn(dataBuffer);
                                                });
                            } else if (fileType.equals(FileType.VIDEO) && !httpRanges.isEmpty()) {
                                HttpRange range = httpRanges.getFirst();
                                long start = range.getRangeStart(fileLength);
                                long end = range.getRangeEnd(fileLength);
                                response.getHeaders().add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength);
                                response.getHeaders().setContentLength(end - start + 1);


                                final long[] rangeStart = {start};
                                final long[] rangeEnd = {end};

                                downloadStream = bytesService.getVideoByRange(file, rangeStart, rangeEnd)
                                        .doOnNext(_ -> {
                                            response.setStatusCode(HttpStatus.PARTIAL_CONTENT);
                                        });
                            } else {
                                response.getHeaders().setContentLength(fileLength);
                            }

                            Flux<DataBuffer> finalDownloadStream = downloadStream
                                    .doFinally(signalType -> {
                                        if (signalType == SignalType.ON_ERROR || signalType == SignalType.CANCEL) {
//                                            log.warn("Stream terminated with signal: {}", signalType);
                                        }
                                    }).doOnDiscard(DataBuffer.class, DataBufferUtils::release);
                            return response.writeWith(finalDownloadStream)
                                    .thenReturn(response)
                                    .onErrorResume(e -> {
                                        response.setStatusCode(HttpStatus.FOUND);
                                        response.getHeaders().setLocation(URI.create("/files/download/" + gridId));
                                        return response.setComplete()
                                                .thenReturn(response);
                                    });
                        }));
    }

    private Mono<Tuple2<Long, String>> saveFileWithIndex(Long index, FilePart filePart, MetadataDto metadataDto) {
        return saveFile(filePart, metadataDto)
                .map(media -> Tuples.of(index, generateFileUrl(media.getGridFsId(), metadataDto.getFileType())));
    }

    @Override
    public Mono<ReactiveGridFsResource> getFile(String gridId) {
        return getFileByGridId(gridId)
                .flatMap(gridFsTemplate::getResource);
    }

    private Mono<GridFSFile> getFileByGridId(String gridId) {
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
                .then(mediaRepository.findAllByGridFsId(gridId)
                        .flatMap(media -> mediaMetadataRepository.deleteAllByMediaId(media.getId()))
                        .then(mediaRepository.deleteAllByGridFsId(gridId)));
    }

    @Override
    public Mono<Void> deleteFiles(List<String> gridIds) {

        List<String> validIds = gridIds.stream().filter(ObjectId::isValid).toList();

        if (validIds.size() != gridIds.size()) {
            log.error("Invalid grid ids: {}", gridIds);
        }

        List<ObjectId> objectIds = validIds.stream().map(ObjectId::new).toList();

        return Mono.zip(
                        gridFsTemplate.delete(new Query(Criteria.where("_id").in(objectIds)))
                                .thenReturn(true),
                        mediaRepository.findAllByGridFsIdIn(gridIds)
                                .map(Media::getId)
                                .collectList()
                                .flatMap(mediaMetadataRepository::deleteAllByMediaIdIn)
                                .then(mediaRepository.deleteAllByGridFsIdIn(gridIds))
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

    }

    @Override
    public Mono<Void> deleteFileWithCacheInvalidate(GridIdsDto ids) {
        return deleteFiles(ids.getGridFsIds())
                .then(imageRedisRepository.deleteAllImagesByGridIds(ids.getGridFsIds()));
    }

    private String generateFileUrl(String id, FileType fileType) {
        return String.format("%s/download/%s",
                fileType.equals(FileType.IMAGE) ? imagesUrl : videosUrl
                , id);
    }

    private Mono<Media> saveFile(FilePart filePart, MetadataDto metadataDto) {
        String mediaType = MediaType.fromFileName(filePart.filename()).getValue();
        Document metadata = new Document("name", metadataDto.getName())
                .append("fileType", metadataDto.getFileType().name())
                .append("mediaType", mediaType);

        return gridFsTemplate.store(filePart.content(), filePart.filename(), Objects.requireNonNull(filePart.headers().getContentType()).toString(), metadata)
                .flatMap(gridFSId -> {
                    Media media = Media.builder()
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


}

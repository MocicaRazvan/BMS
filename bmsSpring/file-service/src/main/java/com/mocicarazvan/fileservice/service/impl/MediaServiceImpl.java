package com.mocicarazvan.fileservice.service.impl;

import com.mocicarazvan.fileservice.dtos.FileUploadResponse;
import com.mocicarazvan.fileservice.dtos.MetadataDto;
import com.mocicarazvan.fileservice.exceptions.FileNotFound;
import com.mocicarazvan.fileservice.models.Media;
import com.mocicarazvan.fileservice.models.MediaMetadata;
import com.mocicarazvan.fileservice.repositories.MediaMetadataRepository;
import com.mocicarazvan.fileservice.repositories.MediaRepository;
import com.mocicarazvan.fileservice.service.MediaService;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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

    @Value("${files.url}")
    private String filesUrl;

    @Override
    public Mono<FileUploadResponse> uploadFiles(Flux<FilePart> files, MetadataDto metadataDto) {
        return files.index()
                .flatMap(indexedFilePart -> saveFileWithIndex(indexedFilePart.getT1(), indexedFilePart.getT2(), metadataDto))
                .collectList()
                .doOnNext(urls -> {
                    log.error("Files uploaded: {}", urls);
                })
                .map(urls -> {
                    urls.sort(Comparator.comparing(Tuple2::getT1));
                    return FileUploadResponse.builder()
                            .files(urls.stream().map(Tuple2::getT2).toList())
                            .fileType(metadataDto.getFileType())
                            .build();
                });

    }

    private Mono<Tuple2<Long, String>> saveFileWithIndex(Long index, FilePart filePart, MetadataDto metadataDto) {
        return saveFile(filePart, metadataDto)
                .map(media -> Tuples.of(index, generateFileUrl(media.getGridFsId())));
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
                .then(mediaRepository.findAllByGridFsId(gridId)
                        .flatMap(media -> mediaMetadataRepository.deleteAllByMediaId(media.getId()))
                        .then(mediaRepository.deleteAllByGridFsId(gridId)));
    }

    @Override
    public Mono<Void> deleteFiles(List<String> gridIds) {
        return Flux.fromIterable(gridIds
                        .stream().filter(ObjectId::isValid)
                        .toList()
                )
                .flatMap(id -> gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(id)))))
                .then(mediaRepository.findAllByGridFsIdIn(gridIds)
                        .flatMap(media -> mediaMetadataRepository.deleteAllByMediaId(media.getId()))
                        .then(mediaRepository.deleteAllByGridFsIdIn(gridIds)))
                .then();
    }

    private String generateFileUrl(String id) {
        return String.format("%s/download/%s", filesUrl, id);
    }

    private Mono<Media> saveFile(FilePart filePart, MetadataDto metadataDto) {
        Document metadata = new Document("name", metadataDto.getName()).append("fileType", metadataDto.getFileType().name());

        return gridFsTemplate.store(filePart.content(), filePart.filename(), Objects.requireNonNull(filePart.headers().getContentType()).toString(), metadata)
                .flatMap(gridFSId -> {
                    Media media = Media.builder()
                            .fileName(metadataDto.getName())
                            .fileType(metadataDto.getFileType().name())
                            .gridFsId(gridFSId.toHexString())
                            .build();
                    return mediaRepository.save(media)
                            .flatMap(m -> mediaMetadataRepository.save(MediaMetadata.builder()
                                            .name(metadataDto.getName())
                                            .mediaId(m.getId())
                                            .build())
                                    .thenReturn(media));
                });
    }


}

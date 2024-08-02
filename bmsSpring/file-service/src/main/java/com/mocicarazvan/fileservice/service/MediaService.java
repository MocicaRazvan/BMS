package com.mocicarazvan.fileservice.service;

import com.mocicarazvan.fileservice.dtos.FileUploadResponse;
import com.mocicarazvan.fileservice.dtos.MetadataDto;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MediaService {

    Mono<FileUploadResponse> uploadFiles(Flux<FilePart> files, MetadataDto metadataDto);

    Mono<ReactiveGridFsResource> getFile(String gridId);

    Mono<GridFSFile> getFileInfo(String gridId);

    Mono<Void> deleteFile(String gridId);

    Mono<Void> deleteFiles(List<String> gridIds);
}

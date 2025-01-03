package com.mocicarazvan.fileservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.fileservice.dtos.FileUploadResponse;
import com.mocicarazvan.fileservice.dtos.GridIdsDto;
import com.mocicarazvan.fileservice.dtos.MetadataDto;
import com.mocicarazvan.fileservice.enums.FileType;
import com.mocicarazvan.fileservice.repositories.ImageRedisRepository;
import com.mocicarazvan.fileservice.service.BytesService;
import com.mocicarazvan.fileservice.service.MediaService;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;


@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final MediaService mediaService;
    private final ObjectMapper objectMapper;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<FileUploadResponse>> uploadFiles(
            @RequestPart("files") Flux<FilePart> files,
            @RequestPart("metadata") String metadata
    ) throws JsonProcessingException {
        MetadataDto parsedMetadata = objectMapper.readValue(metadata, MetadataDto.class);
        return mediaService
                .uploadFiles(files, parsedMetadata)
                .map(response -> ResponseEntity.ok().body(response));
    }


    @GetMapping("/download/{gridId}")
    public Mono<ResponseEntity<Void>> downloadFile(@PathVariable String gridId,
                                                   @RequestParam(required = false) Integer width,
                                                   @RequestParam(required = false) Integer height,
                                                   @RequestParam(required = false) Double quality,
                                                   ServerWebExchange exchange) {
        boolean shouldCheckCache = (width != null || height != null || quality != null);
        return
                mediaService.getResponseForFile(gridId, width, height, quality, exchange, shouldCheckCache).doOnError(e -> log.error("Error getting file", e))
                        .flatMap(response -> Mono.just(new ResponseEntity<>(response.getStatusCode() != null ? response.getStatusCode() : HttpStatus.NOT_FOUND)));
    }


    @GetMapping("/info/{gridId}")
    public Mono<ResponseEntity<GridFSFile>> getFileInfo(@PathVariable String gridId) {
        return mediaService.getFileInfo(gridId)
                .map(file -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(file))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<Void>> deleteFiles(@RequestBody GridIdsDto ids) {
        return mediaService.deleteFileWithCacheInvalidate(ids)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }


}

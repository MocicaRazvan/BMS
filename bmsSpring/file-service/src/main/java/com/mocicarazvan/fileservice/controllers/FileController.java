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
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final MediaService mediaService;
    private final ObjectMapper objectMapper;
    private final BytesService bytesService;
    private final ImageRedisRepository imageRedisRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<FileUploadResponse>> uploadFiles(
            @RequestPart("files") Flux<FilePart> files,
            @RequestPart("metadata") String metadata
    ) throws JsonProcessingException {
        MetadataDto parsedMetadata = objectMapper.readValue(metadata, MetadataDto.class);
        return mediaService.uploadFiles(files, parsedMetadata)
                .map(response -> ResponseEntity.ok().body(response));
    }


    @GetMapping("/download/{gridId}")
    public Mono<ResponseEntity<Void>> downloadFile(@PathVariable String gridId,
                                                   @RequestParam(required = false) Integer width,
                                                   @RequestParam(required = false) Integer height,
                                                   @RequestParam(required = false) Double quality,
                                                   ServerWebExchange exchange) {
        boolean shouldCheckCache = (width != null || height != null || quality != null);
        Mono<ServerHttpResponse> responseMono = shouldCheckCache ?
                imageRedisRepository.getImage(gridId, width, height, quality).flatMap(
                                cachedImage -> {
                                    log.info("Image found in cache");
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
//                        .subscribeOn(Schedulers.boundedElastic())
                        .switchIfEmpty(fetchFileAndProcessFromGridFS(gridId, width, height, quality, exchange)) :
                fetchFileAndProcessFromGridFS(gridId, width, height, quality, exchange);

        return
                responseMono.doOnError(e -> log.error("Error getting file", e))
                        .flatMap(response -> Mono.just(new ResponseEntity<>(response.getStatusCode() != null ? response.getStatusCode() : HttpStatus.NOT_FOUND)));
    }


    private Mono<ServerHttpResponse> fetchFileAndProcessFromGridFS(String gridId, Integer width, Integer height, Double quality, ServerWebExchange exchange) {
        return mediaService.getFile(gridId)
//                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(file -> file.getGridFSFile()
                        .flatMap(gridFSFile -> {
                            FileType fileType = FileType.valueOf(file.getOptions().getMetadata().getString("fileType"));
                            log.info("File type: {}", fileType);
                            ServerHttpRequest request = exchange.getRequest();
                            ServerHttpResponse response = exchange.getResponse();

                            response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"");
                            response.getHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");

                            if (fileType.equals(FileType.VIDEO)) {
                                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "video/mp4");
                            } else if (fileType.equals(FileType.IMAGE)) {
                                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "image/**");
                            }
                            List<HttpRange> httpRanges = request.getHeaders().getRange();
                            Flux<DataBuffer> downloadStream = file.getDownloadStream();
                            long fileLength = gridFSFile.getLength();
                            log.info("Range: " + httpRanges);

                            if (fileType.equals(FileType.IMAGE) && (width != null || height != null || quality != null)) {
                                log.info("file name: {}", file.getFilename());
                                downloadStream =
                                        bytesService.convertWithThumblinator(width, height, quality, downloadStream, response
                                                )
                                                .flatMap(dataBuffer -> {
                                                    response.getHeaders().setContentLength(dataBuffer.readableByteCount());
                                                    return imageRedisRepository.saveImage(gridId, width, height, quality, dataBuffer.toByteBuffer().array())
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

                            return response.writeWith(downloadStream)
                                    .thenReturn(response)
                                    .onErrorResume(e -> {
                                        response.setStatusCode(HttpStatus.FOUND);
                                        response.getHeaders().setLocation(URI.create("/files/download/" + gridId));
                                        return response.setComplete()
                                                .thenReturn(response);
                                    });
                        }));
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
        return mediaService.deleteFiles(ids.getGridFsIds())
                .then(imageRedisRepository.deleteAllImagesByGridIds(ids.getGridFsIds()))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}

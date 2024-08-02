package com.mocicarazvan.fileservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.fileservice.dtos.FileUploadResponse;
import com.mocicarazvan.fileservice.dtos.GridIdsDto;
import com.mocicarazvan.fileservice.dtos.MetadataDto;
import com.mocicarazvan.fileservice.dtos.Range;
import com.mocicarazvan.fileservice.enums.FileType;
import com.mocicarazvan.fileservice.service.MediaService;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;

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
        return mediaService.uploadFiles(files, parsedMetadata)
                .map(response -> ResponseEntity.ok().body(response));
    }


    @GetMapping("/download/{gridId}")
    public Mono<ResponseEntity<Void>> downloadFile(@PathVariable String gridId, ServerWebExchange exchange) {
        return mediaService.getFile(gridId)
                .flatMap(file -> file.getGridFSFile().flatMap(gridFSFile -> {
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

                    if (fileType.equals(FileType.VIDEO) && !httpRanges.isEmpty()) {
                        HttpRange range = httpRanges.get(0);
                        long start = range.getRangeStart(fileLength);
                        long end = range.getRangeEnd(fileLength);
                        response.setStatusCode(HttpStatus.PARTIAL_CONTENT);
                        response.getHeaders().add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength);
                        response.getHeaders().setContentLength(end - start + 1);

                        log.info("Serving range: bytes {}-{} of {}", start, end, fileLength);

                        final long[] rangeStart = {start};
                        final long[] rangeEnd = {end};

                        downloadStream = file.getDownloadStream()
                                .flatMap(dataBuffer -> {
                                    int dataBufferSize = dataBuffer.readableByteCount();
                                    // skip data until the start of the range
                                    if (rangeStart[0] >= dataBufferSize) {
                                        rangeStart[0] -= dataBufferSize;
                                        rangeEnd[0] -= dataBufferSize;
                                        DataBufferUtils.release(dataBuffer);
                                        return Mono.empty();
                                    }

                                    // slice data buffer to fit the start of the range
                                    if (rangeStart[0] > 0) {
                                        int sliceStart = (int) rangeStart[0];
                                        int sliceLength = dataBufferSize - sliceStart;
                                        dataBuffer = dataBuffer.slice(sliceStart, sliceLength);
                                        rangeStart[0] = 0;
                                    }

                                    // slice data buffer to fit the end of the range
                                    if (rangeEnd[0] < dataBufferSize) {
                                        int sliceEnd = (int) (rangeEnd[0] - rangeStart[0] + 1);
                                        if (sliceEnd < dataBuffer.readableByteCount()) {
                                            dataBuffer = dataBuffer.slice(0, sliceEnd);
                                        }
                                        rangeEnd[0] = 0;
                                    } else {
                                        rangeEnd[0] -= dataBufferSize;
                                    }

                                    return Mono.just(dataBuffer);
                                });
                    } else {
                        response.getHeaders().setContentLength(fileLength);
                    }

                    return response.writeWith(downloadStream)
                            .doOnError(e -> log.error("Error writing data", e));
                })).then(Mono.fromCallable(() -> ResponseEntity.ok().build()));
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
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}

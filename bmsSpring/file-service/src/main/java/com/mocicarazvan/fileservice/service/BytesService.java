package com.mocicarazvan.fileservice.service;

import com.mocicarazvan.fileservice.enums.MediaType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;

public interface BytesService {
    Flux<DataBuffer> getVideoByRange(ReactiveGridFsResource file, long start, long end);

    Flux<DataBuffer> convertWithThumblinator(Integer width, Integer height, Double quality, Flux<DataBuffer> downloadStream, MediaType mediaType, Boolean webpOutputEnabled, ServerHttpResponse response);

    Mono<DataBuffer> getImageFallback(ServerHttpResponse response, InputStream imageInputStream);
}

package com.mocicarazvan.fileservice.service;

import com.mocicarazvan.fileservice.enums.MediaType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public interface BytesService {
    Flux<DataBuffer> getVideoByRange(ReactiveGridFsResource file, AtomicLong rangeStart, AtomicLong rangeEnd);

    Flux<DataBuffer> convertWithThumblinator(Integer width, Integer height, Double quality, Flux<DataBuffer> downloadStream, MediaType mediaType, ServerHttpResponse response);

    Mono<DataBuffer> getImageFallback(ServerHttpResponse response, InputStream imageInputStream);
}

package com.mocicarazvan.fileservice.service;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.imageio.stream.ImageInputStream;
import java.io.InputStream;
import java.util.function.Function;

public interface BytesService {
    Flux<DataBuffer> getVideoByRange(ReactiveGridFsResource file, long[] rangeStart, long[] rangeEnd);

    Flux<DataBuffer> convertWithThumblinator(Integer width, Integer height, Double quality, Flux<DataBuffer> downloadStream, ServerHttpResponse response);

    Mono<DataBuffer> getImageFallback(ServerHttpResponse response, InputStream imageInputStream);
}

package com.mocicarazvan.fileservice.repositories;

import reactor.core.publisher.Mono;

import java.util.List;

public interface ImageRedisRepository {
    Mono<Void> saveImage(String gridId, Integer width, Integer height, Double quality, byte[] imageData);

    Mono<byte[]> getImage(String gridId, Integer width, Integer height, Double quality);

    Mono<Void> deleteImage(String gridId, Integer width, Integer height, Double quality);

    String generateCacheKey(String gridId, Integer width, Integer height, Double quality);

    Mono<Void> deleteAllImagesByGridId(String gridId);

    Mono<Void> deleteAllImagesByGridIds(List<String> gridIds);
}

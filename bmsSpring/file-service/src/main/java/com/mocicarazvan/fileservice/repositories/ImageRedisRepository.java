package com.mocicarazvan.fileservice.repositories;

import org.springframework.data.util.Pair;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

public interface ImageRedisRepository {
    Mono<Void> saveImage(String gridId, Integer width, Integer height, Double quality, byte[] imageData, String attch);

    Mono<Tuple2<byte[], String>> getImage(String gridId, Integer width, Integer height, Double quality);

    Mono<Void> deleteImage(String gridId, Integer width, Integer height, Double quality);

    String generateCacheKey(String gridId, Integer width, Integer height, Double quality);

    Mono<Void> deleteAllImagesByGridId(String gridId);

    Mono<Void> deleteAllImagesByGridIds(List<String> gridIds);

    Pair<String, String> generateCacheKeyPair(String gridId, Integer width, Integer height, Double quality);
}

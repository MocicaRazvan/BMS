package com.mocicarazvan.fileservice.repositories;

import com.mocicarazvan.fileservice.dtos.CachedImageRedisModel;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ImageRedisRepository {
    Mono<Void> saveImage(String gridId, Integer width, Integer height, Double quality, Boolean webpOutputEnabled, byte[] imageData, String attch);

    Mono<CachedImageRedisModel> getImage(String gridId, Integer width, Integer height, Double quality, Boolean webpOutputEnabled);

    Mono<Void> deleteImage(String gridId, Integer width, Integer height, Double quality, Boolean webpOutputEnabled);

    String generateCacheKey(String gridId, Integer width, Integer height, Double quality, Boolean webpOutputEnabled);

    Mono<Void> deleteAllImagesByGridId(String gridId);

    Mono<Void> deleteAllImagesByGridIds(List<String> gridIds);

}

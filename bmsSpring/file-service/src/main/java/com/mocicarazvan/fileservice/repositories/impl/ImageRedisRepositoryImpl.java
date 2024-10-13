package com.mocicarazvan.fileservice.repositories.impl;

import com.mocicarazvan.fileservice.repositories.ImageRedisRepository;
import io.lettuce.core.RedisCommandExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ImageRedisRepositoryImpl implements ImageRedisRepository {

    private final ReactiveRedisTemplate<String, byte[]> reactiveByteArrayRedisTemplate;
    private static final String IMAGE_CACHE_KEY_PATTERN = "image:%s:*";
    private static final int SCAN_BATCH_SIZE = 100;


    @Override
    public Mono<Void> saveImage(String gridId, Integer width, Integer height, Double quality, byte[] imageData) {
        return reactiveByteArrayRedisTemplate.opsForValue()
                .set(generateCacheKey(gridId, width, height, quality), imageData)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new RedisCommandExecutionException("Failed to save image")))
                .then();
    }

    @Override
    public Mono<byte[]> getImage(String gridId, Integer width, Integer height, Double quality) {
        log.info("Generated cache key: {}", generateCacheKey(gridId, width, height, quality));

        return reactiveByteArrayRedisTemplate.opsForValue()
                .get(generateCacheKey(gridId, width, height, quality));
    }

    @Override
    public Mono<Void> deleteImage(String gridId, Integer width, Integer height, Double quality) {
        return reactiveByteArrayRedisTemplate.opsForValue()
                .delete(generateCacheKey(gridId, width, height, quality))
                .then();
    }

    @Override
    public Mono<Void> deleteAllImagesByGridId(String gridId) {
        log.error("Deleting pattern: {}", String.format(IMAGE_CACHE_KEY_PATTERN, gridId));
        return scanAndDeleteKeys(String.format(IMAGE_CACHE_KEY_PATTERN, gridId)).then();
    }

    @Override
    public Mono<Void> deleteAllImagesByGridIds(List<String> gridIds) {
        log.error("Deleting images for gridIds: {}", gridIds);
        return Flux.fromIterable(gridIds)
                .doOnNext(id -> log.info("Deleting images for gridId: {}", id))
                .flatMap(this::deleteAllImagesByGridId)
                .then();
    }


    @Override
    public String generateCacheKey(String gridId, Integer width, Integer height, Double quality) {
        return String.format("image:%s:width=%d:height=%d:quality=%.1f", gridId,
                width != null ? width : 0,
                height != null ? height : 0,
                quality != null ? quality : -1.0);
    }

    private Flux<Long> scanAndDeleteKeys(String pattern) {
        return reactiveByteArrayRedisTemplate.scan(ScanOptions.scanOptions().match(pattern).build())
                .buffer(SCAN_BATCH_SIZE)
                .flatMap(keys -> reactiveByteArrayRedisTemplate.delete(Flux.fromIterable(keys)));
    }
}

package com.mocicarazvan.fileservice.repositories.impl;

import com.mocicarazvan.fileservice.dtos.CachedImageRedisModel;
import com.mocicarazvan.fileservice.repositories.ImageRedisRepository;
import io.lettuce.core.RedisCommandExecutionException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

//@Repository
@RequiredArgsConstructor
@Slf4j
//@ConditionalOnProperty(
//        name = "spring.redis.image.cache.enabled",
//        havingValue = "true",
//        matchIfMissing = true
//)
public class ImageRedisRepositoryImpl implements ImageRedisRepository {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private static final String IMAGE_CACHE_KEY_PATTERN = "image:%s:*";
    @Value("${spring.custom.scan.batch.size:25}")
    private int scanBatchSize;

    @Value("${spring.custom.image.cache.expire.minutes:7200}")
    private Long expireMinutes;

    @PostConstruct
    public void init() {
        log.info("Image Redis Repository initialized");
    }


    @Override
    public Mono<Void> saveImage(String gridId, ImageRedisRepositoryImageCharacteristics characteristics, byte[] imageData, String attch) {
        String key = generateCacheKey(gridId, characteristics);
        CachedImageRedisModel cachedImageRedisModel = CachedImageRedisModel.builder()
                .imageData(imageData)
                .attachment(attch)
                .imageCharacteristics(characteristics)
                .build();
        return
                reactiveRedisTemplate.opsForValue()
                        .set(key, cachedImageRedisModel, Duration.ofMinutes(expireMinutes))
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new RedisCommandExecutionException("Failed to save image")))
                        .then();
    }

    @Override
    public Mono<CachedImageRedisModel> getImage(String gridId, ImageRedisRepositoryImageCharacteristics characteristics) {
//        log.info("Generated cache key: {}", generateCacheKey(gridId, width, height, quality));
        String key = generateCacheKey(gridId, characteristics);
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .cast(CachedImageRedisModel.class);

    }

    @Override
    public Mono<Void> deleteImage(String gridId, ImageRedisRepositoryImageCharacteristics characteristics) {
        String key = generateCacheKey(gridId, characteristics);
        return reactiveRedisTemplate.opsForValue()
                .delete(key)
                .then();
    }

    @Override
    public Mono<Void> deleteAllImagesByGridId(String gridId) {
//        log.error("Deleting pattern: {}", String.format(IMAGE_CACHE_KEY_PATTERN, gridId));
        return scanAndDeleteKeys(String.format(IMAGE_CACHE_KEY_PATTERN, gridId)).then();
    }

    @Override
    public Mono<Void> deleteAllImagesByGridIds(List<String> gridIds) {
//        log.error("Deleting images for gridIds: {}", gridIds);
        return Flux.fromIterable(gridIds)
                .doOnNext(id -> log.info("Deleting images for gridId: {}", id))
                .flatMap(this::deleteAllImagesByGridId)
                .then();
    }

    @Override
    public boolean shouldCheckCache(ImageRedisRepositoryImageCharacteristics characteristics) {
        return characteristics.getWidth() != null ||
                characteristics.getHeight() != null ||
                characteristics.getQuality() != null ||
                characteristics.getIsWebpOutputEnabled() != null;

    }


    @Override
    public String generateCacheKey(String gridId, ImageRedisRepositoryImageCharacteristics characteristics) {
        return String.format("image:%s:width=%d:height=%d:quality=%.1f:webpOutputEnabled=%s", gridId,
                characteristics.getWidth() != null ? characteristics.getWidth() : 0,
                characteristics.getHeight() != null ? characteristics.getHeight() : 0,
                characteristics.getQuality() != null ? characteristics.getQuality() : -1.0,
                characteristics.getIsWebpOutputEnabled() != null ? characteristics.getIsWebpOutputEnabled() : false);
    }


    private Flux<Long> scanAndDeleteKeys(String pattern) {
        ScanOptions options = ScanOptions.scanOptions()
                .type(DataType.STRING)
                .count(scanBatchSize)
                .match(pattern).build();
        return reactiveRedisTemplate.scan(options)
                .buffer(scanBatchSize)
                .flatMap(keys -> reactiveRedisTemplate.delete(Flux.fromIterable(keys)));
    }
}

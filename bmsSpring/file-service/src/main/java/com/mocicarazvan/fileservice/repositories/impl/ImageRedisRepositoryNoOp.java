package com.mocicarazvan.fileservice.repositories.impl;

import com.mocicarazvan.fileservice.dtos.CachedImageRedisModel;
import com.mocicarazvan.fileservice.repositories.ImageRedisRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@ConditionalOnProperty(
        name = "spring.redis.image.cache.enabled",
        havingValue = "false"
)
@Slf4j
public class ImageRedisRepositoryNoOp implements ImageRedisRepository {

    @PostConstruct
    public void init() {
        log.info("Image NoOp Cache initialized");
    }

    @Override
    public Mono<Void> saveImage(String gridId, Integer width, Integer height, Double quality, Boolean webpOutputEnabled, byte[] imageData, String attch) {
        return Mono.empty();
    }

    @Override
    public Mono<CachedImageRedisModel> getImage(String gridId, Integer width, Integer height, Double quality, Boolean webpOutputEnable) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteImage(String gridId, Integer width, Integer height, Double quality, Boolean webpOutputEnable) {
        return Mono.empty();
    }

    @Override
    public String generateCacheKey(String gridId, Integer width, Integer height, Double quality, Boolean webpOutputEnable) {
        return "";
    }

    @Override
    public Mono<Void> deleteAllImagesByGridId(String gridId) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteAllImagesByGridIds(List<String> gridIds) {
        return Mono.empty();
    }
}

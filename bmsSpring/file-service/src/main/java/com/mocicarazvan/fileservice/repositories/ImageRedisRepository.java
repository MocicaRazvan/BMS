package com.mocicarazvan.fileservice.repositories;

import com.mocicarazvan.fileservice.dtos.CachedImageRedisModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ImageRedisRepository {
    Mono<Void> saveImage(String gridId, ImageRedisRepositoryImageCharacteristics characteristics, byte[] imageData, String attch);

    Mono<CachedImageRedisModel> getImage(String gridId, ImageRedisRepositoryImageCharacteristics characteristics);

    Mono<Void> deleteImage(String gridId, ImageRedisRepositoryImageCharacteristics characteristics);

    String generateCacheKey(String gridId, ImageRedisRepositoryImageCharacteristics characteristics);

    Mono<Void> deleteAllImagesByGridId(String gridId);

    Mono<Void> deleteAllImagesByGridIds(List<String> gridIds);

    boolean shouldCheckCache(ImageRedisRepositoryImageCharacteristics characteristics);

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    class ImageRedisRepositoryImageCharacteristics {
        private Integer width;
        private Integer height;
        private Double quality;
        private Boolean isWebpOutputEnabled;
    }

}

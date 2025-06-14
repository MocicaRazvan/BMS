package com.mocicarazvan.fileservice.config;


import com.mocicarazvan.fileservice.repositories.ExtendedMediaRepository;
import com.mocicarazvan.fileservice.repositories.ImageRedisRepository;
import com.mocicarazvan.fileservice.repositories.MediaMetadataRepository;
import com.mocicarazvan.fileservice.repositories.MediaRepository;
import com.mocicarazvan.fileservice.repositories.impl.ImageRedisRepositoryImpl;
import com.mocicarazvan.fileservice.service.BytesService;
import com.mocicarazvan.fileservice.service.MediaService;
import com.mocicarazvan.fileservice.service.impl.MediaServiceImpl;
import com.mocicarazvan.fileservice.service.impl.MediaServiceImplWithCache;
import com.mocicarazvan.fileservice.websocket.ProgressWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Configuration
@Slf4j
public class BeanConfig {

    @ConditionalOnProperty(
            name = "spring.redis.image.cache.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    @Bean
    public ImageRedisRepository imageRedisRepository(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        log.info("Registering ImageRedisRepository bean");
        return new ImageRedisRepositoryImpl(reactiveRedisTemplate);
    }

    @ConditionalOnBean(ImageRedisRepository.class)
    @Bean
    public MediaService cachingMediaService(ReactiveGridFsTemplate gridFsTemplate, MediaRepository mediaRepository,
                                            MediaMetadataRepository mediaMetadataRepository, ProgressWebSocketHandler progressWebSocketHandler,
                                            BytesService bytesService, ExtendedMediaRepository extendedMediaRepository, ImageRedisRepository imageRedisRepository) {
        log.info("Registering MediaServiceWithCacheImpl bean with ImageRedisRepository");
        return new MediaServiceImplWithCache(
                gridFsTemplate, mediaRepository, mediaMetadataRepository, progressWebSocketHandler,
                bytesService, extendedMediaRepository, imageRedisRepository
        );
    }

    @ConditionalOnMissingBean(MediaService.class)
    @Bean
    public MediaService mediaService(ReactiveGridFsTemplate gridFsTemplate, MediaRepository mediaRepository,
                                     MediaMetadataRepository mediaMetadataRepository, ProgressWebSocketHandler progressWebSocketHandler,
                                     BytesService bytesService, ExtendedMediaRepository extendedMediaRepository) {
        log.info("Registering MediaServiceImpl bean");
        return new MediaServiceImpl(
                gridFsTemplate, mediaRepository, mediaMetadataRepository, progressWebSocketHandler,
                bytesService, extendedMediaRepository
        );
    }


}

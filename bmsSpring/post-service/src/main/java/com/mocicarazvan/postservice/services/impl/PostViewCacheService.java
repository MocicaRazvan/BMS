package com.mocicarazvan.postservice.services.impl;


import com.mocicarazvan.postservice.repositories.PostViewCountRepository;
import com.mocicarazvan.rediscache.config.FlushProperties;
import com.mocicarazvan.rediscache.services.CacheViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Service
public class PostViewCacheService extends CacheViewService {
    private final PostViewCountRepository postViewCountRepository;

    public PostViewCacheService(ReactiveStringRedisTemplate redisTemplate, PostViewCountRepository postViewCountRepository, FlushProperties flushProperties,
                                @Qualifier("redisAsyncTaskExecutor") SimpleAsyncTaskExecutor asyncTaskExecutor
    ) {
        super(redisTemplate, "post", "post:lock", flushProperties, asyncTaskExecutor);
        this.postViewCountRepository = postViewCountRepository;
    }

    @Override
    public Mono<Void> flushCache(Long itemId, Long count) {
        return postViewCountRepository.incrementViewCount(itemId,
                count,
                LocalDate.now()
        );
    }

    @Override
    protected Mono<Long> getCacheBase(Long itemId, LocalDate accessedStart, LocalDate accessedEnd) {
        return postViewCountRepository.findCountByPostId(itemId, accessedStart, accessedEnd)
                .switchIfEmpty(Mono.just(0L))
                .onErrorResume(_ -> Mono.just(0L));
    }
}

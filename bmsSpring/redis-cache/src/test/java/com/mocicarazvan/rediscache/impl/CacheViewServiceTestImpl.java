package com.mocicarazvan.rediscache.impl;

import com.mocicarazvan.rediscache.config.FlushProperties;
import com.mocicarazvan.rediscache.services.CacheViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.concurrent.Executor;

@Slf4j
public class CacheViewServiceTestImpl extends CacheViewService {
    public CacheViewServiceTestImpl(ReactiveStringRedisTemplate redisTemplate, String itemPrefix, String lockKey, FlushProperties flushProperties, Executor executor) {
        super(redisTemplate, itemPrefix, lockKey, flushProperties, executor);
    }

    @Override
    protected Mono<Void> flushCache(Long itemId, Long count) {
        log.info("Flushing cache with id {} and count {}", itemId, count);
        return Mono.empty();
    }

    @Override
    protected Mono<Long> getCacheBase(Long itemId, LocalDate accessedStart, LocalDate accessedEnd) {
        log.info("Getting cache base with id {} and dates {} {}", itemId, accessedStart, accessedEnd);
        return Mono.just(0L);
    }
}

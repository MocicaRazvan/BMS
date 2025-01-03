package com.mocicarazvan.archiveservice.services.impl;

import com.mocicarazvan.archiveservice.services.SimpleRedisCache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SimpleRedisCacheImpl implements SimpleRedisCache {
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Value("${spring.custom.cache.expireAfterWrite:300}")
    private int expireAfterWrite;

    @Override
    public Mono<Object> getCachedValue(String key) {
        return reactiveRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Mono<Boolean> putCachedValue(String key, Object value) {
        return reactiveRedisTemplate.opsForValue()
                .set(key, value, Duration.ofMinutes(expireAfterWrite));
    }

    @Override
    public Mono<Boolean> evictCachedValue(String key) {
        return reactiveRedisTemplate.delete(key).map(deleted -> deleted > 0);
    }
}

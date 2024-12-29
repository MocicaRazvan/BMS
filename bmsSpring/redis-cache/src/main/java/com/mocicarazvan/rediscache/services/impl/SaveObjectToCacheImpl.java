package com.mocicarazvan.rediscache.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.services.SaveObjectToCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

@RequiredArgsConstructor
public class SaveObjectToCacheImpl implements SaveObjectToCache {
    protected final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    protected final ObjectMapper objectMapper;


    @Override
    public <V, I> Mono<V> getOrSaveObject(I item, Long expireMinutes, Function<I, Mono<V>> cacheMissFunction, Function<I, String> keyFunction, TypeReference<V> typeReference) {
        String key = keyFunction.apply(item);
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .map(result -> objectMapper.convertValue(result, typeReference))
                .switchIfEmpty(cacheMissFunction.apply(item).flatMap(result -> reactiveRedisTemplate.opsForValue()
                        .set(key, result, Duration.ofMinutes(expireMinutes))
                        .thenReturn(result)));
    }
}

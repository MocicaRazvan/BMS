package com.mocicarazvan.archiveservice.services;

import reactor.core.publisher.Mono;

public interface SimpleRedisCache {
    Mono<Object> getCachedValue(String key);

    Mono<Boolean> putCachedValue(String key, Object value);

    Mono<Boolean> evictCachedValue(String key);
}

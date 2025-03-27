package com.mocicarazvan.rediscache.services;

import reactor.core.publisher.Mono;

public interface RedisDistributedLock {
    Mono<Boolean> tryAcquireLock();

    Mono<Boolean> removeLock();
}

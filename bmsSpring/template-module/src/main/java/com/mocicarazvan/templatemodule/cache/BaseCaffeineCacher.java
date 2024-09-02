package com.mocicarazvan.templatemodule.cache;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BaseCaffeineCacher {

    <T> Mono<T> getCachedMono(String key, Mono<T> mono);

    <T> Flux<T> getCachedFlux(String key, Flux<T> flux);

    Mono<Void> invalidateCache(String key);

    Mono<Void> invalidateAllCache();
}

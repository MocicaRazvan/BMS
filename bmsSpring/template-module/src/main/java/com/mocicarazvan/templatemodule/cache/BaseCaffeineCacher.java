package com.mocicarazvan.templatemodule.cache;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

public interface BaseCaffeineCacher<K> {


    @SuppressWarnings("unchecked")
    <T> Mono<T> getCachedMonoOrEmpty(K key);

    @SuppressWarnings("unchecked")
    <T> Flux<T> getCachedFluxOrEmpty(K key);

    <T> Flux<T> cacheFlux(Flux<T> flux);

    <T> Mono<T> cacheMono(Mono<T> mono);

    <T> Flux<T> dangerously_PutAlreadyCachedFlux(K key, Flux<T> flux);

    <T> Mono<T> dangerously_PutAlreadyCachedMono(K key, Mono<T> mono);

    @SuppressWarnings("unchecked")
    <T> Mono<T> getCachedMono(K key, Mono<T> mono);

    @SuppressWarnings("unchecked")
    <T> Flux<T> getCachedFlux(K key, Flux<T> flux);

    Mono<Void> invalidateCache(K key);

    Mono<Void> invalidateAllCache();

    Mono<Void> invalidateAllCache(Iterable<K> keys);

    Set<K> getKeysByCriteria(Predicate<K> criteria);

    ConcurrentMap<K, Publisher<?>> getAsMap();

    Mono<Void> changeKeyForValue(K oldKey, K newKey);
}

package com.mocicarazvan.templatemodule.cache.impl;


import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mocicarazvan.templatemodule.cache.BaseCaffeineCacher;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class BaseCaffeienCacherImpl<K> implements BaseCaffeineCacher<K> {
    private final Integer cacheExpirationTimeMinutes;
    private final Integer cacheMaximumSize;
    private final Duration internalCacheExpirationTime;
    private final AsyncCache<K, Publisher<?>> cacheMap;
    private final Executor executor;


    @Value("${cache.initial.timeout:10}")
    private int cacheInitialTimoutMinutes;

    @Value("${cache.maximum.size:2500}")
    private int cacheInitialMaximumSize;

    public BaseCaffeienCacherImpl(Integer cacheExpirationTimeMinutes, Integer cacheMaximumSize, Executor executor) {
        this.cacheExpirationTimeMinutes = (cacheExpirationTimeMinutes != null) ? cacheExpirationTimeMinutes : cacheInitialTimoutMinutes;
        this.cacheMaximumSize = (cacheMaximumSize != null) ? cacheMaximumSize : cacheInitialMaximumSize;
        this.internalCacheExpirationTime = Duration.ofMinutes(this.cacheExpirationTimeMinutes + 1);
        this.executor = (executor != null) ? executor : Executors.newCachedThreadPool();

        Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
                .maximumSize(this.cacheMaximumSize)
                .executor(this.executor)
                .recordStats();

        int cacheInitialCapacity = (int) (this.cacheMaximumSize * 0.25);
        if (cacheInitialCapacity > 0) {
            cacheBuilder.initialCapacity(cacheInitialCapacity);
        }


        if (this.cacheExpirationTimeMinutes > 0) {
            cacheBuilder.expireAfterAccess(this.cacheExpirationTimeMinutes, TimeUnit.MINUTES);
            cacheBuilder.refreshAfterWrite(this.cacheExpirationTimeMinutes / 2, TimeUnit.MINUTES);
        }

        this.cacheMap = cacheBuilder.buildAsync();
    }

    public BaseCaffeienCacherImpl(Integer cacheExpirationTimeMinutes, Integer cacheMaximumSize) {
        this(cacheExpirationTimeMinutes, cacheMaximumSize, null);
    }

    public BaseCaffeienCacherImpl() {
        this(null, null, null);
    }

    public static <K> BaseCaffeineCacher<K> GetBaseCaffeineCacherWithNoExpirationTime(Integer cacheMaximumSize) {
        return new BaseCaffeienCacherImpl<>(0, cacheMaximumSize, null);
    }

    public static <K> BaseCaffeineCacher<K> GetBaseCaffeineCacherWithNoExpirationTime() {
        return new BaseCaffeienCacherImpl<>(0, null, null);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> Mono<T> getCachedMonoOrEmpty(K key) {
        return Mono.defer(() -> {
            Mono<? extends T> futureCached1 = getMonoFuture(key);
            if (futureCached1 != null) return futureCached1;
            return Mono.empty();
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<? extends T> getMonoFuture(K key) {
        CompletableFuture<Publisher<?>> futureCached = cacheMap.getIfPresent(key);
        if (futureCached != null) {
            log.info("Mono cache hit for key: {}", key);
            return Mono.fromFuture(futureCached).flatMap(cached -> (Mono<T>) cached);
        }
        log.info("Mono cache miss for key: {}", key);
        return null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> Flux<T> getCachedFluxOrEmpty(K key) {
        return Flux.defer(() -> {
            Publisher<T> futureCached1 = getPublisherFuture(key);
            if (futureCached1 != null) return futureCached1;
            return Flux.empty();
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Publisher<T> getPublisherFuture(K key) {
        CompletableFuture<Publisher<?>> futureCached = cacheMap.getIfPresent(key);
        if (futureCached != null) {
            log.info("Flux cache hit for key: {}", key);
            return Mono.fromFuture(futureCached)
                    .flatMapMany(cached -> (Flux<T>) cached);
        }
        log.info("Flux cache miss for key: {}", key);
        return null;
    }


    @Override
    public <T> Flux<T> cacheFlux(Flux<T> flux) {
        return flux.cache(internalCacheExpirationTime);
    }

    @Override
    public <T> Mono<T> cacheMono(Mono<T> mono) {
        return mono.cache(internalCacheExpirationTime);
    }

    @Override
    public <T> Flux<T> dangerously_PutAlreadyCachedFlux(K key, Flux<T> flux) {
        log.error("Dangerously putting for key {}", key);
        cacheMap.put(key, CompletableFuture.completedFuture(flux));
        return flux;
    }

    @Override
    public <T> Mono<T> dangerously_PutAlreadyCachedMono(K key, Mono<T> mono) {
        log.error("Dangerously putting for key {}", key);
        cacheMap.put(key, CompletableFuture.completedFuture(mono));
        return mono;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> Mono<T> getCachedMono(K key, Mono<T> mono) {
        return Mono.defer(() -> {
            Mono<? extends T> futureCached = getMonoFuture(key);
            if (futureCached != null) return futureCached;
            Mono<T> cachedMono = mono.cache(internalCacheExpirationTime);
            cacheMap.put(key, CompletableFuture.completedFuture(cachedMono));
            return cachedMono;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Flux<T> getCachedFlux(K key, Flux<T> flux) {
        return Flux.defer(() -> {
            Publisher<T> futureCached = getPublisherFuture(key);
            if (futureCached != null) return futureCached;
            Flux<T> cachedFlux = flux.cache(internalCacheExpirationTime);
            cacheMap.put(key, CompletableFuture.completedFuture(cachedFlux));
            return cachedFlux;
        });
    }

    //// Invalidate using the cache method, non-blocking as void response
    //asyncCache.synchonous().invalidate(key);
    //
    //// Invalidate using the map method, non-blocking as future
    //var future = asyncCache.asMap().remove(key);
    @Override
    public Mono<Void> invalidateCache(K key) {
        log.info("Invalidating cache for key: {}", key);
        return Mono.fromRunnable(() -> cacheMap.synchronous().invalidate(key));
    }

    @Override
    public Mono<Void> invalidateAllCache() {
        log.error("Invalidating all cache");
        return Mono.fromRunnable(() -> cacheMap.synchronous().invalidateAll());
    }


    @Override
    public Mono<Void> invalidateAllCache(Iterable<K> keys) {
        log.error("Invalidating cache for keys: {}", keys);
        return Mono.fromRunnable(() -> cacheMap.synchronous().invalidateAll(keys));
    }

    @Override
    public Set<K> getKeysByCriteria(Predicate<K> criteria) {
        log.info("Getting keys by criteria");
        return cacheMap.synchronous().asMap().keySet().stream()
                .filter(criteria)
                .collect(Collectors.toSet());
    }

    @Override
    public ConcurrentMap<K, Publisher<?>> getAsMap() {
        log.info("Retrieving cache as a map");
        return cacheMap.synchronous().asMap();
    }

    @Override
    public Mono<Void> changeKeyForValue(K oldKey, K newKey) {
        return Mono.defer(() -> {
            CompletableFuture<Publisher<?>> futureValue = cacheMap.getIfPresent(oldKey);
            if (futureValue != null) {
                return Mono.fromFuture(futureValue)
                        .doOnSuccess(value -> {
                            log.error("Changing key from {} to {}", oldKey, newKey);
                            cacheMap.synchronous().invalidate(oldKey);
                            cacheMap.put(newKey, CompletableFuture.completedFuture(value));
                            log.error("Key changed successfully from {} to {}", oldKey, newKey);
                        })
                        .then();
            } else {
                log.error("Value is null for key: {}", oldKey);
                return invalidateAllCache();
            }
        });
    }

}




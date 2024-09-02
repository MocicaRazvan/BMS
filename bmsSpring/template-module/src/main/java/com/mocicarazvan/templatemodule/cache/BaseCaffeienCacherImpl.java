package com.mocicarazvan.templatemodule.cache;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BaseCaffeienCacherImpl implements BaseCaffeineCacher {
    private Integer cacheExpirationTimeMinutes;
    private Integer cacheMaximumSize;
    private Duration internalCacheExpirationTime;
    private final Cache<String, Publisher<?>> cacheMap;

    public BaseCaffeienCacherImpl(Integer cacheExpirationTimeMinutes, Integer cacheMaximumSize) {
        this.cacheExpirationTimeMinutes = (cacheExpirationTimeMinutes != null) ? cacheExpirationTimeMinutes : 120;
        this.cacheMaximumSize = (cacheMaximumSize != null) ? cacheMaximumSize : 100;
        this.internalCacheExpirationTime = Duration.ofMinutes(this.cacheExpirationTimeMinutes + 1);

        Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
                .maximumSize(this.cacheMaximumSize)
                .recordStats();

        if (this.cacheExpirationTimeMinutes > 0) {
            cacheBuilder.expireAfterWrite(this.cacheExpirationTimeMinutes, TimeUnit.MINUTES);
            cacheBuilder.refreshAfterWrite(this.cacheExpirationTimeMinutes / 2, TimeUnit.MINUTES);
        }

        this.cacheMap = cacheBuilder.build();
    }

    public BaseCaffeienCacherImpl() {
        this(null, null);
    }

    public static BaseCaffeineCacher GetBaseCaffeineCacherWithNoExpirationTime(Integer cacheMaximumSize) {
        return new BaseCaffeienCacherImpl(0, cacheMaximumSize);
    }

    public static BaseCaffeineCacher GetBaseCaffeineCacherWithNoExpirationTime() {
        return new BaseCaffeienCacherImpl(0, null);
    }


    @SuppressWarnings("unchecked")
    public <T> Mono<T> getCachedMono(String key, Mono<T> mono) {
        return Mono.defer(() -> {
            Publisher<?> cached = cacheMap.getIfPresent(key);
            if (cached != null) {
                log.info("Mono cache hit for key: {}", key);
                return (Mono<T>) cached;
            }
            Mono<T> cachedMono = mono.cache(internalCacheExpirationTime);
            cacheMap.put(key, cachedMono);
            return cachedMono;
        });
    }

    @SuppressWarnings("unchecked")
    public <T> Flux<T> getCachedFlux(String key, Flux<T> flux) {
        return Flux.defer(() -> {
            Publisher<?> cached = cacheMap.getIfPresent(key);
            if (cached != null) {
                log.info("Flux cache hit for key: {}", key);
                return (Flux<T>) cached;
            }
            Flux<T> cachedFlux = flux.cache(internalCacheExpirationTime);
            cacheMap.put(key, cachedFlux);
            return cachedFlux;
        });
    }

    public Mono<Void> invalidateCache(String key) {
        return Mono.fromRunnable(() -> cacheMap.invalidate(key));
    }

    public Mono<Void> invalidateAllCache() {
        return Mono.fromRunnable(cacheMap::invalidateAll);
    }
}

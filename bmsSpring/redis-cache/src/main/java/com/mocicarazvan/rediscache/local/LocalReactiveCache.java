package com.mocicarazvan.rediscache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mocicarazvan.rediscache.dtos.CacheRemoveKeyRemoveType;
import com.mocicarazvan.rediscache.dtos.CacheRemoveType;
import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Component
public class LocalReactiveCache implements RemoveFromCache {

    protected final Executor executor;
    protected final Cache<String, Publisher<Object>> cacheMap;
    protected final Long internalExpirationMinutes;
    protected final NotifyLocalRemove notifyLocalRemove;


    public LocalReactiveCache(LocalCacheProperties localCacheProperties,
                              @Qualifier("redisAsyncTaskExecutor") Executor executor, NotifyLocalRemove notifyLocalRemove) {
        this.executor = executor;
        this.internalExpirationMinutes = localCacheProperties.getExpireMinutes() + 1;
        this.notifyLocalRemove = notifyLocalRemove;

        int initialCapacity = (int) (0.1 * localCacheProperties.getMaxSize());
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .maximumSize(localCacheProperties.getMaxSize())
                .initialCapacity(initialCapacity)
                .expireAfterWrite(localCacheProperties.getExpireMinutes(), TimeUnit.MINUTES)
                .executor(executor);

        cacheMap = caffeine.build();
    }

    public void put(String key, Object value) {
        if (value == null) {
            return;
        }
//        log.info("Putting in cache: {} with value: {}", key, value);
        cacheMap.put(removeStar(key), Mono.just(value).cache(Duration.ofMinutes(internalExpirationMinutes)));
    }

    public void put(String key, Collection<Object> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
//        log.info("Putting in cache: {} with values: {}", key, values.size());
        cacheMap.put(removeStar(key), Flux.fromIterable(values).cache(Duration.ofMinutes(internalExpirationMinutes)));
    }

    @Override
    public void removeNotify(String key) {
//        log.info("Removing from cache: {}", key);
        notifyRemoveLocal(key, CacheRemoveKeyRemoveType.NORMAL);
        remove(key);
    }

    @Override
    public void remove(String key) {
//        log.info("Removing from cache: {}", key);
        cacheMap.invalidate(removeStar(key));
    }

    @Override
    public void removeNotify(Collection<String> keys) {
//        log.info("Removing from cache: {}", keys);
        List<String> removedKeys = getKeysToRemove(keys);
        notifyRemoveLocal(removedKeys, CacheRemoveKeyRemoveType.NORMAL);
        cacheMap.invalidateAll(
                removedKeys
        );
    }

    private List<String> getKeysToRemove(Collection<String> keys) {
        return keys.stream()
                .map(this::removeStar)
                .toList();
    }

    @Override
    public void remove(Collection<String> keys) {
//        log.info("Removing from cache: {}", keys);
        List<String> removedKeys = getKeysToRemove(keys);
        cacheMap.invalidateAll(
                removedKeys
        );
    }

    @Override
    public void removeByPrefixNotify(String prefix) {
//        log.info("Removing from cache by prefix: {}", prefix);
        Set<String> keysToRemove = getKeysPrefixByString(prefix);
        notifyRemoveLocal(keysToRemove, CacheRemoveKeyRemoveType.PREFIX);
        cacheMap.invalidateAll(keysToRemove);
    }

    private Set<String> getKeysPrefixByString(String prefix) {
        return cacheMap.asMap().keySet().stream()
                .filter(key -> key.startsWith(removeStar(prefix)))
                .collect(Collectors.toSet());
    }

    @Override
    public void removeByPrefix(String prefix) {
//        log.info("Removing from cache by prefix: {}", prefix);
        Set<String> keysToRemove = getKeysPrefixByString(prefix);
        cacheMap.invalidateAll(keysToRemove);
    }

    @Override
    public void removeByPrefixNotify(Collection<String> prefixes) {
//        log.info("Removing from cache by prefixes: {}", prefixes);
        Set<String> keysToRemove = getKeysPrefixByCollection(prefixes);
        notifyRemoveLocal(keysToRemove, CacheRemoveKeyRemoveType.PREFIX);
        cacheMap.invalidateAll(keysToRemove);
    }

    private Set<String> getKeysPrefixByCollection(Collection<String> prefixes) {
        return cacheMap.asMap().keySet()
                .stream()
                .filter(key -> prefixes.stream()
                        .map(this::removeStar)
                        .anyMatch(key::startsWith))
                .collect(Collectors.toSet());
    }

    @Override
    public void removeByPrefix(Collection<String> prefixes) {
//        log.info("Removing from cache by prefixes: {}", prefixes);
        Set<String> keysToRemove = getKeysPrefixByCollection(prefixes);
        cacheMap.invalidateAll(keysToRemove);
    }


    public Mono<Object> getMonoOrEmpty(String key) {
//        log.info("Getting from cache: {} with value: {}", key, cacheMap.getIfPresent(removeStar(key)));
        Publisher<Object> mono = cacheMap.getIfPresent(removeStar(key));
        if (mono == null) {
            return Mono.empty();
        }
        if (!(mono instanceof Mono)) {
            return Mono.empty();
        }
        return (Mono<Object>) mono;
    }

    public Flux<Object> getFluxOrEmpty(String key) {
//        log.info("Getting from cache: {} with value: {}", key, cacheMap.getIfPresent(removeStar(key)));
        Publisher<Object> flux = cacheMap.getIfPresent(removeStar(key));
        if (flux == null) {
            return Flux.empty();
        }
        if (!(flux instanceof Flux)) {
            return Flux.empty();
        }
        return (Flux<Object>) flux;
    }

    private String removeStar(String key) {
        return key.replace("*", "");
    }

    protected void notifyRemoveLocal(Collection<String> keys, CacheRemoveKeyRemoveType cacheRemoveKeyRemoveType) {
        notifyLocalRemove.notifyRemove(new NotifyCacheRemoveDto(keys, CacheRemoveType.LOCAL, cacheRemoveKeyRemoveType));
    }

    protected void notifyRemoveLocal(String key, CacheRemoveKeyRemoveType cacheRemoveKeyRemoveType) {
        notifyLocalRemove.notifyRemove(new NotifyCacheRemoveDto(key, CacheRemoveType.LOCAL, cacheRemoveKeyRemoveType));
    }
}

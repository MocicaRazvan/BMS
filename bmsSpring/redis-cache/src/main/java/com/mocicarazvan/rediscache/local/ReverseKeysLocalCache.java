package com.mocicarazvan.rediscache.local;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.mocicarazvan.rediscache.dtos.CacheRemoveKeyRemoveType;
import com.mocicarazvan.rediscache.dtos.CacheRemoveType;
import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
//@Component
public class ReverseKeysLocalCache implements RemoveFromCache {
    protected final LoadingCache<String, Set<String>> cacheMap;
    protected final NotifyLocalRemove notifyLocalRemove;

    public ReverseKeysLocalCache(LocalCacheProperties localCacheProperties,
                                 @Qualifier("redisAsyncTaskExecutor") Executor executor, NotifyLocalRemove notifyLocalRemove) {
        this.notifyLocalRemove = notifyLocalRemove;
        int initialCapacity = (int) (0.1 * localCacheProperties.getMaxSize());
        this.cacheMap = Caffeine.newBuilder()
                .maximumSize(Integer.MAX_VALUE)
                .initialCapacity(initialCapacity)
                .expireAfter(new Expiry<String, Set<String>>() {
                    @Override
                    public long expireAfterCreate(String key, Set<String> value, long currentTime) {
                        return TimeUnit.MINUTES.toNanos(localCacheProperties.getExpireMinutes() + 1);
                    }

                    @Override
                    public long expireAfterUpdate(String key, Set<String> value, long currentTime,
                                                  @NonNegative long currentDuration) {
//                        log.info("expireAfterUpdate: key={}, value={}", key, value);
                        return TimeUnit.MINUTES.toNanos(localCacheProperties.getExpireMinutes() + 1);
                    }

                    @Override
                    public long expireAfterRead(String key, Set<String> value, long currentTime,
                                                @NonNegative long currentDuration) {
                        return currentDuration;
                    }
                })
                .executor(executor)
                .build(_ -> new HashSet<>());

    }

    public void put(String key, Collection<String> value) {
        Set<String> set = new HashSet<>(value);
        cacheMap.put(key, set);
    }

    public void add(String key, String value) {
//        log.info("Adding key: {} value: {}", key, value);
        cacheMap.asMap().compute(key, (k, old) -> {
            if (old == null) {
                old = new HashSet<>();
            }
            old.add(value);
//            log.info("add: key={}, value={}", key, value);
            return old;
        });
    }

    public void add(String key, Collection<String> values) {
//        log.info("Adding key: {} values: {}", key, values);
        cacheMap.asMap().compute(key, (k, old) -> {
            if (old == null) {
                old = new HashSet<>(Math.max(values.size(), 16));
            }
            old.addAll(values);
//            log.info("add: key={}, values={}", key, values);
            return old;
        });
    }

    public Set<String> get(String key) {
//        log.info("Getting key: {}", key);
        return Collections.unmodifiableSet(cacheMap.get(key, _ -> new HashSet<>()));
    }

    @Override
    public void removeNotify(Collection<String> keys) {
//        log.info("Removing keys: {}", keys);
        notifyRemoveReverse(keys, CacheRemoveKeyRemoveType.NORMAL);
        cacheMap.invalidateAll(keys);
    }

    @Override
    public void remove(Collection<String> keys) {
//        log.info("Removing keys: {}", keys);
        cacheMap.invalidateAll(keys);
    }

    @Override
    public void removeNotify(String key) {
//        log.info("Removing key: {}", key);
        notifyRemoveReverse(key, CacheRemoveKeyRemoveType.NORMAL);
        cacheMap.invalidate(key);
    }

    @Override
    public void remove(String key) {
//        log.info("Removing key: {}", key);
        cacheMap.invalidate(key);
    }

    @Override
    public void removeByPrefixNotify(String prefix) {
        Set<String> keysToRemove = getKeysStringPrefix(prefix);
        notifyRemoveReverse(keysToRemove, CacheRemoveKeyRemoveType.PREFIX);
        cacheMap.invalidateAll(keysToRemove);
    }

    private Set<String> getKeysStringPrefix(String prefix) {
        return cacheMap.asMap().keySet().stream().filter(k -> k.startsWith(prefix)).collect(Collectors.toSet());
    }

    @Override
    public void removeByPrefix(String prefix) {
        Set<String> keysToRemove = getKeysStringPrefix(prefix);
        cacheMap.invalidateAll(keysToRemove);
    }

    @Override
    public void removeByPrefixNotify(Collection<String> prefixes) {
        Set<String> keysToRemove = getKeysPrefixCollection(prefixes);
        notifyRemoveReverse(keysToRemove, CacheRemoveKeyRemoveType.PREFIX);
        cacheMap.invalidateAll(keysToRemove);
    }

    @Override
    public void removeByPrefix(Collection<String> prefixes) {
        Set<String> keysToRemove = getKeysPrefixCollection(prefixes);
        cacheMap.invalidateAll(keysToRemove);
    }

    private Set<String> getKeysPrefixCollection(Collection<String> prefixes) {
        return cacheMap.asMap().keySet().stream().filter(k -> prefixes.stream().anyMatch(k::startsWith))
                .collect(Collectors.toSet());
    }


    protected void notifyRemoveReverse(Collection<String> keys, CacheRemoveKeyRemoveType cacheRemoveKeyRemoveType) {
        notifyLocalRemove.notifyRemove(new NotifyCacheRemoveDto(keys, CacheRemoveType.REVERSE, cacheRemoveKeyRemoveType));
    }

    protected void notifyRemoveReverse(String key, CacheRemoveKeyRemoveType cacheRemoveKeyRemoveType) {
        notifyLocalRemove.notifyRemove(new NotifyCacheRemoveDto(key, CacheRemoveType.REVERSE, cacheRemoveKeyRemoveType));
    }

}

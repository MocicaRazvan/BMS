package com.mocicarazvan.rediscache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mocicarazvan.rediscache.dtos.CacheRemoveKeyRemoveType;
import com.mocicarazvan.rediscache.dtos.CacheRemoveType;
import com.mocicarazvan.rediscache.dtos.NotifyCacheRemoveDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
//@Component
public class ReverseKeysLocalCache implements RemoveFromCache {
    protected final Cache<String, CopyOnWriteArrayList<String>> cacheMap;
    protected final NotifyLocalRemove notifyLocalRemove;

    public ReverseKeysLocalCache(LocalCacheProperties localCacheProperties,
                                 @Qualifier("redisAsyncTaskExecutor") Executor executor, NotifyLocalRemove notifyLocalRemove) {
        this.notifyLocalRemove = notifyLocalRemove;
        int initialCapacity = (int) (0.1 * localCacheProperties.getMaxSize());
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .maximumSize(Integer.MAX_VALUE)
                .initialCapacity(initialCapacity)
                .expireAfterWrite(localCacheProperties.getExpireMinutes() + 1, TimeUnit.MINUTES)
                .executor(executor);

        this.cacheMap = caffeine.build();
    }

    public void put(String key, Collection<String> value) {
        cacheMap.put(key, new CopyOnWriteArrayList<>(value));
    }

    public void add(String key, String value) {
//        log.info("Adding key: {} value: {}", key, value);
        cacheMap.asMap().compute(key, (k, oldList) -> {
            //to reset the list if it is null
            if (oldList == null) {
                return new CopyOnWriteArrayList<>(List.of(value));
            }
            CopyOnWriteArrayList<String> newList = new CopyOnWriteArrayList<>(oldList);
            newList.add(value);
            return newList;
        });
    }

    private CopyOnWriteArrayList<String> getListFromCache(String key) {
        return cacheMap.get(key, _ -> new CopyOnWriteArrayList<>());
    }

    public void add(String key, Collection<String> values) {
//        log.info("Adding key: {} values: {}", key, values);
        cacheMap.asMap().compute(key, (k, oldList) -> {
            if (oldList == null) {
                return new CopyOnWriteArrayList<>(values);
            }
            CopyOnWriteArrayList<String> newList = new CopyOnWriteArrayList<>(oldList);
            newList.addAll(values);
            return newList;
        });
    }

    public List<String> get(String key) {
//        log.info("Getting key: {}", key);
        return getListFromCache(key);
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

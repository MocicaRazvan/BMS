package com.mocicarazvan.rediscache.services;

import com.mocicarazvan.rediscache.config.FlushProperties;
import com.mocicarazvan.rediscache.services.impl.RedisDistributedLockImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.Executor;

@Slf4j
public abstract class CacheViewService {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final String viewKeyPrefix;
    private final String getKeyPrefix;
    private final FlushProperties flushProperties;
    private final Executor executor;
    private final RedisDistributedLock redisDistributedLock;
    private Disposable scheduledFlush;

    public CacheViewService(ReactiveStringRedisTemplate redisTemplate, String itemPrefix, String lockKey, FlushProperties flushProperties, Executor executor) {
        this.redisTemplate = redisTemplate;
        this.viewKeyPrefix = itemPrefix + ":views:";
        this.getKeyPrefix = itemPrefix + ":viewsGet:";
        this.flushProperties = flushProperties;
        this.executor = executor;
        this.redisDistributedLock = new RedisDistributedLockImpl(lockKey, 5 * flushProperties.getTimeout() - 1, redisTemplate);
        log.info("Cache view service started for: {} with flush timeout: {}", itemPrefix, flushProperties.getTimeout());
    }

    @PostConstruct
    protected void scheduleFlush() {

        if (!flushProperties.isEnabled()) {
            log.info("Flush is disabled for: {}", viewKeyPrefix);
            return;
        }

        scheduledFlush = Flux.interval(Duration.ofSeconds(flushProperties.getTimeout()))
                .flatMap(_ -> redisDistributedLock.tryAcquireLock())
                .flatMap(acquired -> {
                    if (!acquired) return Mono.empty();
                    return scanKeys(viewKeyPrefix + "*")
                            .publishOn(Schedulers.fromExecutor(executor))
                            .flatMap(this::flushViewCountForKey, flushProperties.getParallelism())
                            .onErrorContinue((throwable, o) -> {
                                log.error("Error while flushing view count for key: {}", o, throwable);
                            })
                            .then(redisDistributedLock.removeLock());
                })
                .subscribe();

    }

    @PreDestroy
    protected void cleanup() {
        if (scheduledFlush != null && !scheduledFlush.isDisposed()) {
            scheduledFlush.dispose();
        }
    }

    public Mono<Long> incrementView(Long itemId) {
        return redisTemplate.opsForValue().increment(viewKeyPrefix + itemId);
    }


    protected Flux<String> scanKeys(String pattern) {
        return redisTemplate.scan(
                ScanOptions.scanOptions()
                        .match(pattern)
                        .count(100)
                        .type(DataType.STRING)
                        .build()
        );
    }

    protected abstract Mono<Void> flushCache(Long itemId, Long count);

    protected abstract Mono<Long> getCacheBase(Long itemId, LocalDate accessedStart, LocalDate accessedEnd);

    public Mono<Long> getCache(Long itemId, LocalDate accessedStart, LocalDate accessedEnd) {
        String key = getKeyPrefix + itemId + ":" + accessedStart + ":" + accessedEnd;
        return redisTemplate.opsForValue().get(key)
                .filter(Objects::nonNull)
                .filter(s -> !s.equalsIgnoreCase("null"))
                .map(Long::valueOf)
                .switchIfEmpty(
                        Mono.defer(() -> getCacheBase(itemId, accessedStart, accessedEnd)
                                .flatMap(cnt ->
                                        redisTemplate.opsForValue()
                                                .set(key, cnt.toString(), Duration.ofSeconds(flushProperties.getTimeout() / 2))
                                                .thenReturn(cnt)
                                ))
                );
    }

    protected Mono<Long> flushViewCountForKey(String key) {
        return redisTemplate.opsForValue().get(key)
                .filter(Objects::nonNull)
                .flatMap(cnt -> {
                    Long itemId = Long.parseLong(key.replace(viewKeyPrefix, ""));
                    Long count = Long.parseLong(cnt);
                    return flushCache(itemId, count)
                            .then(redisTemplate.delete(key));
                });
    }

}

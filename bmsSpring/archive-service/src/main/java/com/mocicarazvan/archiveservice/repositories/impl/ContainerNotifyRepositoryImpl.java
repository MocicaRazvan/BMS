package com.mocicarazvan.archiveservice.repositories.impl;

import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import com.mocicarazvan.archiveservice.repositories.ContainerNotifyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


@Repository
@RequiredArgsConstructor
public class ContainerNotifyRepositoryImpl implements ContainerNotifyRepository {
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private static final String KEY_NAME = "archive:queue:container:action:all";

    @Value("${spring.custom.cache.expireAfterWrite:5184000}")
    private int expireAfterWrite;

    @Override
    public Mono<Boolean> addNotification(NotifyContainerAction notifyContainerAction) {
        double score = notifyContainerAction.getTimestamp().toEpochSecond(ZoneOffset.UTC);

        return reactiveRedisTemplate.opsForZSet()
                .add(KEY_NAME, notifyContainerAction, score)
                .then(reactiveRedisTemplate.expire(KEY_NAME, Duration.ofSeconds(expireAfterWrite * 2L)));
    }

    @Override
    public Flux<NotifyContainerAction> getNotifications() {
        return reactiveRedisTemplate.opsForZSet()
                .rangeByScore(
                        KEY_NAME,
                        Range.from(Range.Bound.inclusive(getCutoffScore()))
                                .to(Range.Bound.unbounded())
                )
                .cast(NotifyContainerAction.class);
    }

    @Override
    public Mono<Long> invalidateNotifications() {
        return reactiveRedisTemplate.opsForZSet()
                .removeRangeByScore(
                        KEY_NAME,
                        Range.from(Range.Bound.inclusive(0.0))
                                .to(Range.Bound.exclusive(getCutoffScore()))
                );
    }

    private double getCutoffScore() {
        return LocalDateTime.now()
                .minusSeconds(expireAfterWrite)
                .toEpochSecond(ZoneOffset.UTC);
    }

}

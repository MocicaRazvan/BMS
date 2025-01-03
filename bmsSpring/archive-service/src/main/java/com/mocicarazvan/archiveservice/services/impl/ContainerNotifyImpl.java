package com.mocicarazvan.archiveservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.archiveservice.dtos.enums.ContainerAction;
import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import com.mocicarazvan.archiveservice.services.ContainerNotify;
import com.mocicarazvan.archiveservice.services.SimpleRedisCache;
import com.mocicarazvan.archiveservice.websocket.BatchHandler;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ContainerNotifyImpl implements ContainerNotify {
    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final SimpleRedisCache simpleRedisCache;

    public ContainerNotifyImpl(ObjectMapper objectMapper, ReactiveRedisTemplate<String, String> redisTemplate, SimpleRedisCache simpleRedisCache) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.simpleRedisCache = simpleRedisCache;

    }

    @Override
    public void notifyContainersStartCron(String queueName) {
        simpleRedisCache.evictCachedValue(queueName)
                .then(sendContainerActionToRedis(queueName, ContainerAction.START_CRON))
                .subscribe();
    }

    @Override
    public void notifyContainersStartManual(String queueName) {
        simpleRedisCache.evictCachedValue(queueName)
                .then(sendContainerActionToRedis(queueName, ContainerAction.START_MANUAL))
                .subscribe();

    }

    @Override
    public void notifyContainersStop(String queueName) {
        simpleRedisCache.evictCachedValue(queueName)
                .then(sendContainerActionToRedis(queueName, ContainerAction.STOP))
                .subscribe();
    }


    private Mono<Void> sendContainerActionToRedis(String queueName, ContainerAction containerAction) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(NotifyContainerAction.builder()
                        .action(containerAction)
                        .queueName(queueName)
                        .id(UUID.randomUUID().toString())
                        .timestamp(LocalDateTime.now())
                        .build()))
                .flatMap(json -> redisTemplate.convertAndSend(BatchHandler.getChannelName(), json))
                .then();
    }
}

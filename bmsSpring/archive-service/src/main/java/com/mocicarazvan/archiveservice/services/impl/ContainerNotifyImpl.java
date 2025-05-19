package com.mocicarazvan.archiveservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.archiveservice.dtos.enums.ContainerAction;
import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import com.mocicarazvan.archiveservice.services.ContainerActionPublisher;
import com.mocicarazvan.archiveservice.services.ContainerNotify;
import com.mocicarazvan.archiveservice.services.ContainerNotifyService;
import com.mocicarazvan.archiveservice.services.SimpleRedisCache;
import com.mocicarazvan.archiveservice.websocket.BatchHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class ContainerNotifyImpl implements ContainerNotify {
    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final SimpleRedisCache simpleRedisCache;
    private final ContainerNotifyService containerNotifyService;
    private final ContainerActionPublisher containerActionPublisher;

    public ContainerNotifyImpl(
            ObjectMapper objectMapper, ReactiveRedisTemplate<String, String> redisTemplate, SimpleRedisCache simpleRedisCache,
            ContainerNotifyService containerNotifyService, ContainerActionPublisher containerActionPublisher) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.simpleRedisCache = simpleRedisCache;
        this.containerNotifyService = containerNotifyService;
        this.containerActionPublisher = containerActionPublisher;
    }

    @Override
    public void notifyContainersStartCron(String queueName) {
        simpleRedisCache.evictCachedValue(queueName)
                .then(sendContainerActionToRedis(queueName, ContainerAction.START_CRON))
                .subscribe();
    }

    @Override
    public void notifyContainersStartManual(String queueName) {
        log.info("Notifying container to start manually for queue: {}", queueName);
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
        return Mono.just(NotifyContainerAction.builder()
                        .action(containerAction)
                        .queueName(queueName)
                        .id(UUID.randomUUID().toString())
                        .timestamp(LocalDateTime.now())
                        .build())
                .flatMap(ca -> Mono.zip(Mono.fromCallable(() ->
                                        objectMapper.writeValueAsString(ca)),
                                containerNotifyService.addNotification(ca),
                                containerActionPublisher.sendContainerActionMessage(ca)
                        ).map(Tuple2::getT1)
                )
                .flatMap(json -> redisTemplate.convertAndSend(BatchHandler.getChannelName(), json))
                .then();
    }
}

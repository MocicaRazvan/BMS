package com.mocicarazvan.archiveservice.services.impl;


import com.mocicarazvan.archiveservice.config.QueuesPropertiesConfig;
import com.mocicarazvan.archiveservice.dtos.websocket.NotifyBatchUpdate;
import com.mocicarazvan.archiveservice.services.BatchNotify;
import com.mocicarazvan.archiveservice.services.SimpleRedisCache;
import com.mocicarazvan.archiveservice.utils.MonoWrapper;
import com.mocicarazvan.archiveservice.websocket.BatchHandler;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BatchNotifyWS implements BatchNotify {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ConcurrentMap<String, Long> queueMap;
    private final ConcurrentMap<String, AtomicReference<ScheduledFuture<?>>> scheduledTasks;
    private final ConcurrentMap<String, Instant> lastReceived;
    private final SimpleAsyncTaskScheduler taskExecutor;
    private final SimpleRedisCache simpleRedisCache;

    @Value("${spring.custom.batch.notify.timeout:15}")
    private int notifyTimeout;

    @Value("${spring.custom.batch.update.period:3}")
    private int updatePeriod;


    public BatchNotifyWS(ReactiveRedisTemplate<String, Object> redisTemplate, QueuesPropertiesConfig queuesPropertiesConfig,
                         @Qualifier("redisSimpleAsyncTaskScheduler") SimpleAsyncTaskScheduler taskExecutor,
                         SimpleRedisCache simpleRedisCache) {
        this.redisTemplate = redisTemplate;
        this.taskExecutor = taskExecutor;
        this.queueMap = new ConcurrentHashMap<>(
                queuesPropertiesConfig.getQueues().size()
        );
        this.scheduledTasks = queuesPropertiesConfig.getQueues().stream().collect(
                Collectors.toConcurrentMap(
                        queue -> queue,
                        _ -> new AtomicReference<>()
                )
        );
        this.lastReceived = new ConcurrentHashMap<>(
                queuesPropertiesConfig.getQueues().size()
        );
        this.simpleRedisCache = simpleRedisCache;
    }

    @Override
    public <T> void notifyBatchUpdate(List<T> items, String queueName) {

        MonoWrapper.wrapBlockingFunction(() -> {
            queueMap.merge(queueName, (long) items.size(), Long::sum);
            lastReceived.put(queueName, Instant.now());

            startScheduledTaskIfNotStarted(queueName);
        });
    }


    private void startScheduledTaskIfNotStarted(String queueName) {
//        log.info("Sending update global outside count is {}", globalCnt);
        ScheduledFuture<?> queueFuture = scheduledTasks.computeIfAbsent(queueName,
                _ -> new AtomicReference<>()
        ).get();
        if (queueFuture == null || queueFuture.isCancelled()) {
            PeriodicTrigger trigger = new PeriodicTrigger(Duration.ofSeconds(updatePeriod));
            trigger.setInitialDelay(Duration.ofSeconds(updatePeriod / 2));
            ScheduledFuture<?> scheduledFuture = taskExecutor.schedule(() -> handleScheduledTask(queueName),
                    trigger);
            assert scheduledFuture != null;
            scheduledTasks.put(queueName, new AtomicReference<>(scheduledFuture));
        }
    }

    private void handleScheduledTask(String queueName) {
        queueMap.computeIfPresent(queueName, (_, count) -> {
            if (count > 0L) {
                sendUpdateBatchToRedis(queueName, count, false);
            }
            return 0L;
        });
        unscheduleQueue(queueName);
    }

    private void unscheduleQueue(String queueName) {
        Instant lastUpdateTime = lastReceived.get(queueName);
        if (lastUpdateTime != null && lastUpdateTime.isBefore(Instant.now().minusSeconds(notifyTimeout))) {
            log.info("Last received is {}", lastReceived.get(queueName));
            queueMap.put(queueName, 0L);
            ScheduledFuture<?> scheduledFuture = scheduledTasks.get(queueName).get();
            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                sendUpdateBatchToRedis(queueName, 0L, true);
            }
            stopScheduledTask(queueName);
            log.info("Stopped task for queue {}", queueName);

        }
    }

    private void sendUpdateBatchToRedis(String queueName, Long count, boolean finished) {
        try {
            Mono.zip(simpleRedisCache.evictCachedValue(queueName),
                            redisTemplate.convertAndSend(BatchHandler.getChannelName(),
                                    NotifyBatchUpdate.builder()
                                            .numberProcessed(count)
                                            .queueName(queueName)
                                            .id(UUID.randomUUID().toString())
                                            .timestamp(LocalDateTime.now())
                                            .finished(finished)
                                            .build()))
                    .subscribe();
        } catch (Exception e) {
            throw new RuntimeException("Error sending message to redis", e);
        }


    }


    private void stopScheduledTask(String queueName) {
        ScheduledFuture<?> scheduledFuture = scheduledTasks.get(queueName).get();
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
            scheduledTasks.put(queueName, new AtomicReference<>());
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduledTasks.values().forEach(task -> {
            if (!task.get().isCancelled()) {
                task.get().cancel(true);
            }
        });
        scheduledTasks.clear();
    }
}

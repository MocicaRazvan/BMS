package com.mocicarazvan.archiveservice.services.impl;


import com.mocicarazvan.archiveservice.config.rabbit.QueuesPropertiesConfig;
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


@Slf4j
@Service
public class BatchNotifyWS implements BatchNotify {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ConcurrentMap<String, Long> queueMap;
    private final ConcurrentMap<String, ScheduledFuture<?>> scheduledTasks;
    private final ConcurrentMap<String, Instant> lastReceived;
    private final SimpleAsyncTaskScheduler taskExecutor;
    private final SimpleRedisCache simpleRedisCache;
    // no mutable state in this class, so no need for unique instances
    private final PeriodicTrigger periodicTrigger;

    @Value("${spring.custom.batch.notify.timeout:15}")
    private int notifyTimeout;


    public BatchNotifyWS(ReactiveRedisTemplate<String, Object> redisTemplate, QueuesPropertiesConfig queuesPropertiesConfig,
                         @Qualifier("redisSimpleAsyncTaskScheduler") SimpleAsyncTaskScheduler taskExecutor,
                         SimpleRedisCache simpleRedisCache,
                         @Value("${spring.custom.batch.update.period:3}") int updatePeriod
    ) {
        this.redisTemplate = redisTemplate;
        this.taskExecutor = taskExecutor;
        this.queueMap = new ConcurrentHashMap<>(
                queuesPropertiesConfig.getQueues().size()
        );
        this.scheduledTasks = new ConcurrentHashMap<>(
                queuesPropertiesConfig.getQueues().size()
        );
        this.lastReceived = new ConcurrentHashMap<>(
                queuesPropertiesConfig.getQueues().size()
        );
        this.simpleRedisCache = simpleRedisCache;
        periodicTrigger = new PeriodicTrigger(Duration.ofSeconds(updatePeriod));
        periodicTrigger.setInitialDelay(Duration.ofSeconds(updatePeriod / 2));

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
        scheduledTasks.compute(queueName,
                (qn, existingFuture) -> {
                    if (existingFuture == null || existingFuture.isCancelled()) {
                        return taskExecutor.schedule(() -> handleScheduledTask(qn), periodicTrigger);
                    } else {
                        return existingFuture;
                    }
                });
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
            log.info("Last received is {}", lastUpdateTime);
            stopScheduledTask(queueName);
            Long pendingCount = queueMap.getOrDefault(queueName, 0L);
            queueMap.put(queueName, 0L);
            sendUpdateBatchToRedis(queueName, pendingCount, true);
            log.info("Stopped task for queue {}", queueName);
        }
    }

    private void sendUpdateBatchToRedis(String queueName, Long count, boolean finished) {
        Mono.zip(simpleRedisCache.evictCachedValue(queueName)
                                .doOnError(e -> log.error("Error evicting cache for queue {}", queueName, e)),
                        redisTemplate.convertAndSend(BatchHandler.getChannelName(),
                                        NotifyBatchUpdate.builder()
                                                .numberProcessed(count)
                                                .queueName(queueName)
                                                .id(UUID.randomUUID().toString())
                                                .timestamp(LocalDateTime.now())
                                                .finished(finished)
                                                .build())
                                .doOnError(e -> log.error("Error sending notification for queue {}", queueName, e))
                )
                .doOnError(e -> log.error("Error in batch‐update zip for {}", queueName, e))
                .subscribe();
    }


    private void stopScheduledTask(String queueName) {
        ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(queueName);
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduledTasks.values().forEach(task -> {
            if (task != null && !task.isCancelled()) {
                task.cancel(true);
            }
        });
        scheduledTasks.clear();
    }
}

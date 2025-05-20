package com.mocicarazvan.archiveservice.services.impl;


import com.mocicarazvan.archiveservice.config.rabbit.QueuesPropertiesConfig;
import com.mocicarazvan.archiveservice.dtos.QueueInformationWithTimestamp;
import com.mocicarazvan.archiveservice.exceptions.QueueNameNotValid;
import com.mocicarazvan.archiveservice.schedulers.ContainerScheduler;
import com.mocicarazvan.archiveservice.services.QueueService;
import com.mocicarazvan.archiveservice.services.SimpleRedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueServiceImpl implements QueueService {
    private final RabbitAdmin rabbitAdmin;
    private final QueuesPropertiesConfig queuesPropertiesConfig;
    private final Map<String, ContainerScheduler> containerSchedulers;
    private final SimpleRedisCache simpleRedisCache;


    @Override
    public Mono<QueueInformationWithTimestamp> getQueueInfo(String queueName, boolean refresh) {
        if (refresh) {
            return fetchQueueInfoFromSource(queueName)
                    .flatMap(queueInfo -> simpleRedisCache.putCachedValue(queueName, queueInfo)
                            .thenReturn(queueInfo));
        }

        return simpleRedisCache.getCachedValue(queueName)
                .cast(QueueInformationWithTimestamp.class)
                .switchIfEmpty(Mono.defer(() -> fetchQueueInfoFromSource(queueName)
                        .flatMap(queueInfo -> simpleRedisCache.putCachedValue(queueName, queueInfo)
                                .thenReturn(queueInfo))));
    }

    @Override
    public Mono<String> evictCache(String queueName) {
        return simpleRedisCache.evictCachedValue(queueName)
                .thenReturn(queueName);
    }

    @Override
    public Mono<QueueInformationWithTimestamp> startContainerForFixedTime(String queueName, long aliveMillis) {

        return operateContainer(queueName,
                containerScheduler -> containerScheduler.startContainerForFixedTime(aliveMillis));
    }


    @Override
    public Mono<QueueInformationWithTimestamp> stopContainer(String queueName) {
        return operateContainer(queueName, ContainerScheduler::stopContainerManually);
    }

    private Mono<QueueInformationWithTimestamp> operateContainer(String queueName, Consumer<ContainerScheduler> operation) {
        ContainerScheduler containerScheduler = containerSchedulers.get(queueName);
        if (containerScheduler == null) {
            return Mono.error(new QueueNameNotValid("Queue name not valid"));
        }

        operation.accept(containerScheduler);

        return getQueueInfo(queueName, true);
    }


    private Mono<QueueInformationWithTimestamp> fetchQueueInfoFromSource(String queueName) {
        return Mono.justOrEmpty(rabbitAdmin.getQueueInfo(queueName))
                .switchIfEmpty(Mono.just(new QueueInformation(queueName, 0, 0)))
                .map(qi -> QueueInformationWithTimestamp.fromQueueInformation(qi, queuesPropertiesConfig.getQueueJob(queueName)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}

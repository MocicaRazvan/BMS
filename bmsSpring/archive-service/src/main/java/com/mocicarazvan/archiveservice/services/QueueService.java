package com.mocicarazvan.archiveservice.services;


import com.mocicarazvan.archiveservice.dtos.QueueInformationWithTimestamp;
import reactor.core.publisher.Mono;

public interface QueueService {

    Mono<QueueInformationWithTimestamp> getQueueInfo(String queueName, boolean refresh);

    Mono<String> evictCache(String queueName);


    Mono<QueueInformationWithTimestamp> startContainerForFixedTime(String queueName, long aliveMillis);

    Mono<QueueInformationWithTimestamp> stopContainer(String queueName);
}

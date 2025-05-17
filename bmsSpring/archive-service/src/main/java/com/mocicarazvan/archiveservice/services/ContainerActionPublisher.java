package com.mocicarazvan.archiveservice.services;

import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import reactor.core.publisher.Mono;

public interface ContainerActionPublisher {
    Mono<Long> sendContainerActionMessage(NotifyContainerAction action);
}

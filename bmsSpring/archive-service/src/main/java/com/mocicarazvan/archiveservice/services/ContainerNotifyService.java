package com.mocicarazvan.archiveservice.services;

import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContainerNotifyService {
    Mono<Boolean> addNotification(NotifyContainerAction notifyContainerAction);

    Flux<NotifyContainerAction> getNotifications();

    Mono<Long> invalidateNotifications();

}

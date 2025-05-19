package com.mocicarazvan.archiveservice.services;

import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface UserContainerActionService {
    Flux<NotifyContainerAction> getModelsForUser(String userId);

    Mono<Boolean> saveFromActionAndUserId(NotifyContainerAction action, String userId);

    Mono<Void> deleteAllByUserIdAndActionIdIn(String userId, Collection<String> actionIds);

    Flux<NotifyContainerAction> getInitialData(ServerWebExchange exchange);
}

package com.mocicarazvan.archiveservice.services.impl;

import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import com.mocicarazvan.archiveservice.mappers.NotifyContainerActionMapper;
import com.mocicarazvan.archiveservice.models.UserContainerAction;
import com.mocicarazvan.archiveservice.repositories.UserContainerActionRepository;
import com.mocicarazvan.archiveservice.services.UserContainerActionService;
import com.mocicarazvan.archiveservice.utils.UserHeaderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserContainerActionServiceImpl implements UserContainerActionService {
    private final UserContainerActionRepository userContainerActionRepository;
    private final NotifyContainerActionMapper notifyContainerActionMapper;

    @Override
    public Flux<NotifyContainerAction> getModelsForUser(String userId) {
        return userContainerActionRepository.findAllNotificationsByUserId(userId)
                .map(notifyContainerActionMapper::fromModelToAction);
    }

    @Override
    public Mono<Boolean> saveFromActionAndUserId(NotifyContainerAction action, String userId) {
        return userContainerActionRepository.save(
                UserContainerAction.builder()
                        .actionId(action.getId())
                        .userId(userId)
                        .build()
        ).map(Objects::nonNull);
    }

    @Override
    public Mono<Void> deleteAllByUserIdAndActionIdIn(String userId, Collection<String> actionIds) {
        return userContainerActionRepository.deleteAllByUserIdAndActionIdIn(userId, actionIds);
    }

    @Override
    public Flux<NotifyContainerAction> getInitialData(ServerWebExchange exchange) {
        String userId = UserHeaderUtils.getFromServerWebExchange(exchange);
        if (userId == null || userId.isEmpty()) {
            return Flux.empty();
        }

        return getModelsForUser(userId);

    }

}

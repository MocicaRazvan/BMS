package com.mocicarazvan.archiveservice.controllers;

import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import com.mocicarazvan.archiveservice.services.ContainerNotifyService;
import com.mocicarazvan.archiveservice.services.NotifyContainerModelService;
import com.mocicarazvan.archiveservice.services.UserContainerActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/archive")
@RequiredArgsConstructor
public class ArchiveNotificationsController {
    private final ContainerNotifyService containerNotifyService;
    private final UserContainerActionService userContainerActionService;
    private final NotifyContainerModelService notifyContainerModelService;

    @GetMapping(value = "/container/notifications", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<NotifyContainerAction> getNotifications() {
        return containerNotifyService.getNotifications();
    }

    @DeleteMapping(value = "/container/notifications", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    public Mono<ResponseEntity<Long>> invalidateNotifications() {
        return containerNotifyService.invalidateNotifications()
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/actions/notifications", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<NotifyContainerAction> getActionsNotificationsForUser(ServerWebExchange exchange) {
        return userContainerActionService.getInitialData(exchange);
    }

    @DeleteMapping(value = "/actions/delete-notifications", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    public Mono<ResponseEntity<Void>> deleteOldNotifications() {
        return notifyContainerModelService.deleteOldNotifyContainerModels()
                .then(Mono.just(ResponseEntity.ok().build()));
    }

}

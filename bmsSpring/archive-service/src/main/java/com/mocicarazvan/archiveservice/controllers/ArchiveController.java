package com.mocicarazvan.archiveservice.controllers;


import com.mocicarazvan.archiveservice.dtos.QueueInformationWithTimestamp;
import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import com.mocicarazvan.archiveservice.repositories.ContainerNotifyRepository;
import com.mocicarazvan.archiveservice.services.QueueService;
import com.mocicarazvan.archiveservice.validators.annotations.ValidQueueName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private final QueueService queueService;
    private final ContainerNotifyRepository containerNotifyRepository;

    @GetMapping(value = "/queue", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    public Mono<ResponseEntity<QueueInformationWithTimestamp>> getQueue(
            @RequestParam(value = "queueName") @Valid @ValidQueueName String queueName,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh
    ) {
        return queueService.getQueueInfo(queueName, refresh).map(ResponseEntity::ok);
    }

    @PatchMapping(value = "/queue/evict", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    public Mono<ResponseEntity<String>> evictCache(
            @RequestParam(value = "queueName") @Valid @ValidQueueName String queueName
    ) {
        return queueService.evictCache(queueName).map(ResponseEntity::ok);
    }

    @PatchMapping("/container/schedule")
    public Mono<ResponseEntity<QueueInformationWithTimestamp>> startQueueContainer(
            @RequestParam(value = "queueName") @Valid @ValidQueueName String queueName,
            @RequestParam(value = "alive") @Valid @Min(10000) @Max(60000000) long alive
    ) {
        return queueService.startContainerForFixedTime(queueName, alive).map(ResponseEntity::ok);
    }

    @PatchMapping("/container/stop")
    public Mono<ResponseEntity<QueueInformationWithTimestamp>> stopQueueContainer(
            @RequestParam(value = "queueName") @Valid @ValidQueueName String queueName
    ) {
        return queueService.stopContainer(queueName).map(ResponseEntity::ok);
    }

    @GetMapping(value = "/container/notifications", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<NotifyContainerAction> getNotifications() {
        return containerNotifyRepository.getNotifications();
    }

    @DeleteMapping(value = "/container/notifications", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    public Mono<ResponseEntity<Long>> invalidateNotifications() {
        return containerNotifyRepository.invalidateNotifications()
                .map(ResponseEntity::ok);
    }

}

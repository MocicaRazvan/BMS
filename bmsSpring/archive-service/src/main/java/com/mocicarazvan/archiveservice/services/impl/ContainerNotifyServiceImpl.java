package com.mocicarazvan.archiveservice.services.impl;

import com.mocicarazvan.archiveservice.dtos.websocket.NotifyContainerAction;
import com.mocicarazvan.archiveservice.mappers.NotifyContainerActionMapper;
import com.mocicarazvan.archiveservice.repositories.NotifyContainerModelRepository;
import com.mocicarazvan.archiveservice.services.ContainerNotifyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ContainerNotifyServiceImpl implements ContainerNotifyService {
    private final NotifyContainerModelRepository notifyContainerModelRepository;
    private final NotifyContainerActionMapper notifyContainerActionMapper;
    private final Duration cutoffDuration;

    public ContainerNotifyServiceImpl(NotifyContainerModelRepository notifyContainerModelRepository,
                                      NotifyContainerActionMapper notifyContainerActionMapper,
                                      @Value("${notify.container.action.cutoff.duration:5184000}") int cutoffDurationInSeconds) {

        this.notifyContainerModelRepository = notifyContainerModelRepository;
        this.notifyContainerActionMapper = notifyContainerActionMapper;
        this.cutoffDuration = Duration.ofSeconds(cutoffDurationInSeconds);
    }


    @Override
    public Mono<Boolean> addNotification(NotifyContainerAction notifyContainerAction) {
        return notifyContainerModelRepository
                .save(notifyContainerActionMapper.fromActionToModel(notifyContainerAction))
                .map(Objects::nonNull);
    }

    @Override
    public Flux<NotifyContainerAction> getNotifications() {
        return notifyContainerModelRepository
                .findAll(Sort.by("timeStamp").descending())
                .map(notifyContainerActionMapper::fromModelToAction);
    }

    @Override
    public Mono<Long> invalidateNotifications() {
        return notifyContainerModelRepository
                .deleteAllByTimestampBefore(LocalDateTime.now().minus(cutoffDuration))
                .then(notifyContainerModelRepository.count());
    }
}

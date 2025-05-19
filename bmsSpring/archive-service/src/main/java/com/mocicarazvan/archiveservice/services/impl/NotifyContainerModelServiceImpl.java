package com.mocicarazvan.archiveservice.services.impl;

import com.mocicarazvan.archiveservice.repositories.NotifyContainerModelRepository;
import com.mocicarazvan.archiveservice.services.NotifyContainerModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
public class NotifyContainerModelServiceImpl implements NotifyContainerModelService {
    private final NotifyContainerModelRepository notifyContainerModelRepository;
    private final Duration cutoffDuration;

    public NotifyContainerModelServiceImpl(NotifyContainerModelRepository notifyContainerModelRepository,
                                           @Value("${notify.container.action.cutoff.duration:5184000}") int cutoffDurationInSeconds) {
        this.notifyContainerModelRepository = notifyContainerModelRepository;
        this.cutoffDuration = Duration.ofSeconds(cutoffDurationInSeconds);
    }


    @Override
    public Mono<Void> deleteOldNotifyContainerModels() {
        return notifyContainerModelRepository.deleteAllByTimestampBefore(LocalDateTime.now().minus(cutoffDuration))
                .doOnError(e -> log.error("Error deleting old NotifyContainerModels: {}", e.getMessage()))
                .doOnSuccess(v -> log.info("Deleted old NotifyContainerModels successfully"));
    }
}

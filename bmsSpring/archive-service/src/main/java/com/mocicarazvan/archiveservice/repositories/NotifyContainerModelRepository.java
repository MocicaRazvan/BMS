package com.mocicarazvan.archiveservice.repositories;

import com.mocicarazvan.archiveservice.models.NotifyContainerModel;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface NotifyContainerModelRepository extends R2dbcRepository<NotifyContainerModel, String> {
    Mono<Void> deleteAllByTimestampBefore(LocalDateTime timestampBefore);
}

package com.mocicarazvan.archiveservice.services;

import reactor.core.publisher.Mono;

public interface NotifyContainerModelService {
    Mono<Void> deleteOldNotifyContainerModels();
}

package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import reactor.core.publisher.Mono;

public interface CountInParentService {
    Mono<EntityCount> countInParent(Long childId);
}

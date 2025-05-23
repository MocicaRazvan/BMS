package com.mocicarazvan.templatemodule.repositories;

import reactor.core.publisher.Mono;

import java.util.Collection;

public interface CountIds {
    Mono<Long> countByIds(Collection<Long> ids);
}

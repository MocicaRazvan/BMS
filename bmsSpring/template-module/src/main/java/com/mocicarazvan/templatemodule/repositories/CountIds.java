package com.mocicarazvan.templatemodule.repositories;

import reactor.core.publisher.Flux;

import java.util.Collection;

public interface CountIds {
    Flux<Long> countByIds(Collection<Long> ids);
}

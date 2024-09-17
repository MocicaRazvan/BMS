package com.mocicarazvan.templatemodule.repositories;

import reactor.core.publisher.Flux;

import java.util.List;

public interface CountIds {
    Flux<Long> countByIds(List<Long> ids);
}

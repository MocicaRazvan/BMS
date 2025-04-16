package com.mocicarazvan.templatemodule.hateos;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ReactiveRepresentationModelAssembler<T> {
//    Mono<EntityModel<T>> toModel(T entity);

    Mono<CustomEntityModel<T>> toModel(T entity);
}

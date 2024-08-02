package com.mocicarazvan.templatemodule.hateos.user;

import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import reactor.core.publisher.Mono;

public interface ReactiveRepresentationModelAssembler<T> {
//    Mono<EntityModel<T>> toModel(T entity);

    Mono<CustomEntityModel<T>> toModel(T entity);
}

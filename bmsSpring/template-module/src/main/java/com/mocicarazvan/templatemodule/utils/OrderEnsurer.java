package com.mocicarazvan.templatemodule.utils;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class OrderEnsurer {

    public static <T, L> Flux<T> orderFlux(Flux<T> flux, List<L> ids,
                                           Function<T, L> idExtractor
    ) {
        return flux
                .collectMap(idExtractor)
                .flatMapMany(
                        map -> Flux.fromIterable(ids)
                                .map(map::get)
                                .filter(Objects::nonNull)
                );
    }
}

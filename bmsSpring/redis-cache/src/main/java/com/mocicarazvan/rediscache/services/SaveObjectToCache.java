package com.mocicarazvan.rediscache.services;

import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface SaveObjectToCache {
    <V, I> Mono<V> getOrSaveObject(I item, Long expireMinutes, Function<I, V> cacheMissFunction, Function<I, String> keyFunction, TypeReference<V> typeReference);

}

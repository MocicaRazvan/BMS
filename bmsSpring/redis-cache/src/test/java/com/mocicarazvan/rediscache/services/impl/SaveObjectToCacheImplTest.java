package com.mocicarazvan.rediscache.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.config.LocalCacheConfig;
import com.mocicarazvan.rediscache.services.SaveObjectToCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Function;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveObjectToCacheImplTest {

    @Mock
    ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Mock
    LocalCacheConfig localCacheConfig;

    @Mock
    ReactiveValueOperations<String, Object> valueOps;

    @Mock
    ObjectMapper objectMapper;

    SaveObjectToCache saveObjectToCacheImpl;

    record Dummy(String name) {
    }

    @BeforeEach
    void setUp() {
        saveObjectToCacheImpl = new SaveObjectToCacheImpl(reactiveRedisTemplate, objectMapper);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);

    }


    @Test
    void shouldReturnCachedValueWhenExists() {
        Dummy item = new Dummy("name");
        String key = "key";
        TypeReference<Dummy> typeReference = new TypeReference<>() {
        };
        Function<Dummy, String> keyFunction = i -> key;
        Function<Dummy, Mono<Dummy>> cacheMissFunction = mock(Function.class);

        when(reactiveRedisTemplate.opsForValue().get(key)).thenReturn(Mono.just(item));
        when(objectMapper.convertValue(eq(item), any(TypeReference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<Dummy> result = saveObjectToCacheImpl.getOrSaveObject(item, 10L, cacheMissFunction, keyFunction, typeReference);

        StepVerifier.create(result)
                .expectNext(item)
                .verifyComplete();

        verify(cacheMissFunction, never()).apply(any());
    }

    @Test
    void shouldCallCacheMissAndCacheResultWhenNotPresent() {
        Dummy item = new Dummy("name");
        Dummy miss = new Dummy("miss");
        String key = "key";
        TypeReference<Dummy> typeReference = new TypeReference<>() {
        };
        Function<Dummy, String> keyFunction = i -> key;
        Function<Dummy, Mono<Dummy>> cacheMissFunction = mock(Function.class);
        when(cacheMissFunction.apply(any())).thenReturn(Mono.just(miss));

        when(reactiveRedisTemplate.opsForValue().get(key)).thenReturn(Mono.empty());
        when(reactiveRedisTemplate.opsForValue().set(key, miss, Duration.ofMinutes(10L))).thenReturn(Mono.just(true));


        Mono<Dummy> result = saveObjectToCacheImpl.getOrSaveObject(item, 10L, cacheMissFunction, keyFunction, typeReference);

        StepVerifier.create(result)
                .expectNext(miss)
                .verifyComplete();

        verify(cacheMissFunction, times(1)).apply(item);
        verify(objectMapper, never()).convertValue(any(), any(TypeReference.class));
    }

    @Test
    void shouldPropagateErrorWhenCacheMissFunctionFails() {

        Dummy item = new Dummy("name");
        String key = "key";
        TypeReference<Dummy> typeReference = new TypeReference<>() {
        };
        Function<Dummy, String> keyFunction = i -> key;
        Function<Dummy, Mono<Dummy>> cacheMissFunction = mock(Function.class);
        when(cacheMissFunction.apply(any())).thenReturn(Mono.error(new RuntimeException("error")));

        when(reactiveRedisTemplate.opsForValue().get(key)).thenReturn(Mono.empty());

        Mono<Dummy> result = saveObjectToCacheImpl.getOrSaveObject(item, 10L, cacheMissFunction, keyFunction, typeReference);

        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof RuntimeException && e.getMessage().equals("error")
                )
                .verify();

        verify(objectMapper, never()).convertValue(any(), any(TypeReference.class));

    }

}
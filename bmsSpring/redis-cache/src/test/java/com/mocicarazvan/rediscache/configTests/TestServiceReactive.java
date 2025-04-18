package com.mocicarazvan.rediscache.configTests;

import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCacheEvict;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@NoArgsConstructor
public class TestServiceReactive {
    private final List<Dummy> dummies = List.of(new Dummy(1L, "a", 1), new Dummy(2L, "b", 2), new Dummy(3L, "c", 3));
    private final List<Dummy> dummiesHuge = IntStream.range(1, 1_000)
            .mapToObj(_ -> dummies)
            .flatMap(List::stream)
            .toList();
    public static final String CACHE_KEY = "dummies";

    @RedisReactiveCache(key = CACHE_KEY, id = "#id")
    public Mono<Dummy> getDummyById(Long id) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }

    @RedisReactiveCache(key = CACHE_KEY, id = "#id", saveToCache = false)
    public Mono<Dummy> getDummyByIdNoSave(Long id) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }

    @RedisReactiveCache(key = CACHE_KEY)
    public Mono<Dummy> getDummyByIdNoIdAnn(Long id) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }

    @RedisReactiveCache(key = CACHE_KEY, idPath = "id")
    public Flux<Dummy> getAllDummies() {
        return Flux.fromIterable(dummies);
    }


    @RedisReactiveCache(key = CACHE_KEY, idPath = "id")
    public Flux<Dummy> getAllDummiesHuge() {
        return Flux.fromIterable(getDummiesHuge());
    }

    @RedisReactiveCache(key = CACHE_KEY, idPath = "id")
    public Flux<Dummy> getNDummies(int n) {
        return Flux.fromIterable(dummies.subList(0, n));
    }

    @RedisReactiveCache(key = CACHE_KEY, idPath = "id")
    public Flux<Dummy> getAllDummiesWithDelay() {
        return Flux.fromIterable(dummies).delayElements(Duration.ofSeconds(1));
    }

    @RedisReactiveCache(key = CACHE_KEY, idPath = "id", saveToCache = false)
    public Flux<Dummy> getAllDummiesNoSave() {
        return Flux.fromIterable(dummies);
    }

    @RedisReactiveCache(key = CACHE_KEY)
    public Flux<Dummy> getAllDummiesNoIdAnn() {
        return Flux.fromIterable(dummies);
    }

    @RedisReactiveCacheEvict(key = CACHE_KEY, id = "#id")
    public Mono<Dummy> evictDummyById(Long id) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }

    @RedisReactiveCacheEvict(key = CACHE_KEY)
    public Mono<Dummy> evictDummyByIdInvalidId(Long id) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }


    @RedisReactiveCache(key = CACHE_KEY, id = "id")
    public List<Dummy> invalidReturnType(Long id) {
        return dummies;
    }

    @RedisReactiveCacheEvict(key = CACHE_KEY, id = "id")
    public List<Dummy> invalidReturnTypeEvict(Long id) {
        return dummies;
    }

    public List<Dummy> getDummiesHuge() {
        return IntStream.range(1, 1_000)
                .mapToObj(_ -> dummies)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public static record Dummy(Long id, String name, int age) {
    }
}

package com.mocicarazvan.rediscache.configTests;

import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Data
@NoArgsConstructor
public class TestServiceChildReactive {
    private final List<DummyWithMaster> dummies = List.of(new DummyWithMaster(1L, "a", 1, "1"),
            new DummyWithMaster(2L, "b", 2, "2"), new DummyWithMaster(3L, "c", 3, "3"));
    public static final String CACHE_KEY = "dummies";

    @RedisReactiveChildCache(key = CACHE_KEY, id = "#id")
    public Mono<DummyWithMaster> getDummyByIdDefaultMaster(Long id) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }

    @RedisReactiveChildCache(key = CACHE_KEY, id = "#id", masterId = "#masterId")
    public Mono<DummyWithMaster> getDummyByIdMaster(Long id, String masterId) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }


    @RedisReactiveChildCache(key = CACHE_KEY, idPath = "id")
    public Flux<DummyWithMaster> getAllDummiesDefaultMaster() {
        return Flux.fromIterable(dummies);
    }

    @RedisReactiveChildCache(key = CACHE_KEY, idPath = "id", masterId = "#masterId")
    public Flux<DummyWithMaster> getAllDummiesMaster(String masterId) {
        return Flux.fromIterable(dummies);
    }

    @RedisReactiveChildCacheEvict(key = CACHE_KEY, masterId = "#masterId")
    public Mono<Boolean> evictAllDummiesMasterId(String masterId) {
        return Mono.just(true);
    }

    @RedisReactiveChildCacheEvict(key = CACHE_KEY, masterPath = "masterId")
    public Mono<DummyWithMaster> evictAllDummiesMasterPath() {
        return Mono.just(dummies.get(0));
    }


    @RedisReactiveChildCacheEvict(key = CACHE_KEY, id = "#id")
    public Mono<DummyWithMaster> evictDummyByIdDefaultMaster(Long id) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }

    @RedisReactiveChildCacheEvict(key = CACHE_KEY, id = "#id", masterId = "#masterId")
    public Mono<DummyWithMaster> evictDummyByIdMaster(Long id, String masterId) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }

    @RedisReactiveChildCacheEvict(key = CACHE_KEY, masterPath = "#inv")
    public Mono<DummyWithMaster> evictDummyByIdInvalidArgs(Long id) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }

    @RedisReactiveChildCacheEvict(key = CACHE_KEY)
    public Mono<DummyWithMaster> evictDummyByIdInvalidAllDefault(Long id) {
        return Mono.justOrEmpty(dummies.stream().filter(d -> d.id().equals(id)).findFirst());
    }


    @RedisReactiveChildCache(key = CACHE_KEY, id = "id")
    public List<DummyWithMaster> invalidReturnType(Long id) {
        return dummies;
    }

    @RedisReactiveChildCacheEvict(key = CACHE_KEY, id = "#id")
    public List<DummyWithMaster> invalidReturnTypeEvict(Long id) {
        return dummies;
    }

    public record DummyWithMaster(Long id, String name, Integer value, String masterId) {
    }

}

package com.mocicarazvan.rediscache.configTests;

import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCacheEvict;
import com.mocicarazvan.rediscache.enums.BooleanEnum;
import lombok.Data;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Data
public class TestServiceApprovedReactive {
    public static final String CACHE_KEY = "dummies";

    public static final List<DummyWithApproved> dummies = List.of(
            new DummyWithApproved(1L, "Dummy 1", 1, true, -1L),
            new DummyWithApproved(2L, "Dummy 2", 2, false, 1L),
            new DummyWithApproved(3L, "Dummy 3", 3, true, 0L)
    );

    @RedisReactiveApprovedCache(key = CACHE_KEY)
    public Flux<DummyWithApproved> getDummyDefaultApp(Long id) {
        return Flux.fromIterable(dummies);
    }

    @RedisReactiveApprovedCache(key = CACHE_KEY, idPath = "id")
    public Flux<DummyWithApproved> getDummiesDefaults() {
        return Flux.fromIterable(dummies);
    }

    @RedisReactiveApprovedCache(key = CACHE_KEY, idPath = "id", approved = BooleanEnum.FALSE)
    public Flux<DummyWithApproved> getDummiesApprovedFalse() {
        return Flux.fromIterable(
                dummies.stream().filter(d -> !d.approved)
                        .toList()
        );
    }

    @RedisReactiveApprovedCache(key = CACHE_KEY, idPath = "id", approved = BooleanEnum.TRUE)
    public Flux<DummyWithApproved> getDummiesApprovedTrue() {
        return Flux.fromIterable(
                dummies.stream().filter(DummyWithApproved::approved)
                        .toList()
        );
    }

    @RedisReactiveApprovedCache(key = CACHE_KEY, approvedArgumentPath = "#approved", idPath = "id")
    public Flux<DummyWithApproved> getDummiesApprovedArgPath(boolean approved) {
        return Flux.fromIterable(
                dummies.stream().filter(d -> d.approved == approved)
                        .toList()
        );
    }

    @RedisReactiveApprovedCache(key = CACHE_KEY, idPath = "id", forWhom = "0")
    public Flux<DummyWithApproved> getDummiesForWhomZero() {
        return Flux.fromIterable(dummies);
    }

    @RedisReactiveApprovedCache(key = CACHE_KEY, idPath = "id", forWhom = "#forWhom")
    public Flux<DummyWithApproved> getDummiesForWhomPath(Long forWhom) {
        return Flux.fromIterable(dummies);
    }

    @RedisReactiveApprovedCache(key = CACHE_KEY, id = "#id")
    public Mono<DummyWithApproved> getDummyById(Long id) {
        return Mono.justOrEmpty(dummies.stream()
                .filter(d -> d.id.equals(id))
                .findFirst());
    }

    @RedisReactiveApprovedCacheEvict(key = CACHE_KEY, id = "#id", forWhomPath = "forWhom")
    public Mono<Pair<DummyWithApproved, Boolean>> invalidateCache(Long id, boolean b) {
        return Mono.just(
                Pair.of(dummies.stream()
                        .filter(d -> d.id.equals(id))
                        .findFirst()
                        .orElseThrow(), b)
        );
    }

    @RedisReactiveApprovedCache(key = CACHE_KEY, id = "#id")
    public DummyWithApproved invalidGetReturnType(Long id) {
        return dummies.stream()
                .filter(d -> d.id.equals(id))
                .findFirst()
                .orElseThrow();
    }

    @RedisReactiveApprovedCacheEvict(key = CACHE_KEY, id = "#id")
    public Mono<Long> invalidateInvalidReturnType(Long id) {
        return Mono.just(id);
    }

    public record DummyWithApproved(Long id, String name, Integer value, boolean approved, Long forWhom) {
    }

}

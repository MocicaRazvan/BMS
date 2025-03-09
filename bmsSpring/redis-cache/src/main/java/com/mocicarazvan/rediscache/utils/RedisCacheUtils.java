package com.mocicarazvan.rediscache.utils;


import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.List;

//@Component
public class RedisCacheUtils {
    protected final AspectUtils aspectUtils;
    protected final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    public RedisCacheUtils(AspectUtils aspectUtils, ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.aspectUtils = aspectUtils;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public String createReverseIndexKey(String key, Long id) {
        return key + ":" + id;
    }

    public void checkValidId(String idSpel) {
        if (idSpel == null || idSpel.isBlank()) {
            throw new RuntimeException("RedisReactiveCacheAdd: Annotated method has invalid idSpel, expected idSpel not null or empty");
        }
    }

    public String getSingleKey(String key, Long annId) {
        return key + ":single:" + annId;
    }

    public String getListKey(String key) {
        return key + ":list";
    }

    public String getHashKey(String argsHash) {
        return ":hash:" + argsHash;
    }

    public Flux<String> getActualKeys(List<String> patterns, ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        return Flux.fromIterable(patterns)
                .flatMap(p -> reactiveRedisTemplate
                        .scan(
                                ScanOptions.scanOptions()
                                        .type(DataType.STRING)
                                        .count(50)
                                        .match(p).build()
                        ));
    }

    public Tuple3<Flux<String>, Mono<Long>, Long> getOptionalIdDelete(ProceedingJoinPoint joinPoint, String key, String idSpel) {
        Flux<String> members = Flux.empty();
        Mono<Long> zipWith = Mono.empty();
        Long annId = -100L;
        if (idSpel != null && !idSpel.isBlank()) {
            annId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(idSpel, joinPoint));
            members = reactiveRedisTemplate.opsForSet().members(createReverseIndexKey(key, annId)).cast(String.class);
            zipWith = reactiveRedisTemplate.delete(createReverseIndexKey(key, annId));

        }
        return Tuples.of(members, zipWith, annId);
    }

    public Mono<Long> deleteListFromRedis(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Mono.just(0L);
        }
        return reactiveRedisTemplate.delete(keys.toArray(String[]::new))
                .defaultIfEmpty(0L);
    }
}

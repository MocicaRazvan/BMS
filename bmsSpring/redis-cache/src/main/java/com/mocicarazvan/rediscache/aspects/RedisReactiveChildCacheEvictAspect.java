package com.mocicarazvan.rediscache.aspects;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisChildCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Aspect
//@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, ObjectMapper.class, AspectUtils.class})
public class RedisReactiveChildCacheEvictAspect extends RedisReactiveCacheEvictAspect {
    protected final RedisChildCacheUtils redisChildCacheUtils;

    @Value("${spring.custom.scan.batch.size:50}")
    private int scanBatchSize;

    public RedisReactiveChildCacheEvictAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate, AspectUtils aspectUtils, RedisChildCacheUtils redisChildCacheUtils) {
        super(reactiveRedisTemplate, aspectUtils, redisChildCacheUtils);
        this.redisChildCacheUtils = redisChildCacheUtils;
    }

    @Around("execution(* *(..)) && @annotation(com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict)")
    public Object redisReactiveCacheChildEvict(ProceedingJoinPoint joinPoint) {
        Method method = aspectUtils.getMethod(joinPoint);
        Class<?> returnType = method.getReturnType();
        RedisReactiveChildCacheEvict annotation = method.getAnnotation(RedisReactiveChildCacheEvict.class);
        String key = aspectUtils.extractKeyFromAnnotation(annotation.key(), joinPoint);
        String idSpel = annotation.id();
        String masterIdSpel = annotation.masterId();
        Long masterId = null;
        String masterPath = annotation.masterPath();
        if (masterIdSpel != null && !masterIdSpel.isBlank()) {
            masterId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(masterIdSpel, joinPoint), -1L);
        }
        Long annId = null;
        if (idSpel != null && !idSpel.isBlank()) {
            annId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(idSpel, joinPoint));
        }

        if (annId == null && masterId == null && (masterPath == null || masterPath.isBlank())) {
            throw new RuntimeException("redisReactiveCacheChildEvict: Annotated method has invalid arguments, expected at least one of id, masterId, masterPath to be present");
        }

        if (returnType.isAssignableFrom(Mono.class)) {

            if (masterPath != null && !(masterPath.isBlank())) {
                return methodMonoResponseToCacheByMasterPath(joinPoint, key, annId, masterPath);
            }


            return invalidateForChild(key, annId, masterId)
                    .then(methodResponse(joinPoint));
        }

        throw new RuntimeException("redisReactiveCacheChildEvict: Annotated method has invalid return type, expected return type to be Mono<?>");

    }

    protected Mono<Object> methodMonoResponseToCacheByMasterPath(ProceedingJoinPoint joinPoint, String key, Long annId, String masterPath) {
        try {
            return ((Mono<Object>) joinPoint.proceed(joinPoint.getArgs()))
                    .flatMap(methodResponse -> invalidateForChildMasterPath(joinPoint, key, annId, masterPath, methodResponse)
                            .doOnNext(t -> log.info("METHOD RESPONSE: " + methodResponse))
                            .thenReturn(methodResponse)
                    );

        } catch (Throwable e) {
            return Mono.error(e);
        }
    }

    protected Mono<Long> invalidateForChildMasterPath(ProceedingJoinPoint joinPoint, String key, Long annId, String masterPath, Object methodResponse) {
        Long masterId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpressionForObject(masterPath, methodResponse, joinPoint));
        return invalidateForChild(key, annId, masterId);
    }

    protected Mono<Long> invalidateForChild(String key, Long id, Long masterId) {
        Flux<String> keysFromReverse = Flux.empty();
        Mono<Long> zipReverse = Mono.empty();
        if (id != null) {
            keysFromReverse = reactiveRedisTemplate.opsForSet().members(redisChildCacheUtils.createReverseIndexKey(key, id)).cast(String.class);
            zipReverse = reactiveRedisTemplate.delete(redisChildCacheUtils.createReverseIndexKey(key, id));
        }


        return
                reactiveRedisTemplate.delete(Flux.concat(keysFromReverse,
                                keysToInvalidateByMaster(key, masterId))
                        ).defaultIfEmpty(0L)
                        .zipWith(zipReverse.defaultIfEmpty(0L))
                        .map(t -> t.getT1() + t.getT2());


    }

    protected Flux<String> keysToInvalidateByMaster(String key, Long masterId) {
        if (masterId == null) {
            return Flux.empty();
        }
        List<String> patterns = new ArrayList<>();
        patterns.add(redisChildCacheUtils.getMasterKey(key, masterId) + "*");
        if (masterId != -1L) {
            patterns.add(redisChildCacheUtils.getMasterKey(key, -1L) + "*");
        }
//        log.info("keysToInvalidateByMaster to invalidate: " + patterns);
        return Flux.fromIterable(patterns)
                .flatMap(p -> reactiveRedisTemplate
                        .scan(
                                ScanOptions.scanOptions()
                                        .type(DataType.STRING)
                                        .count(scanBatchSize)
                                        .match(p).build()
                        ));
    }
}

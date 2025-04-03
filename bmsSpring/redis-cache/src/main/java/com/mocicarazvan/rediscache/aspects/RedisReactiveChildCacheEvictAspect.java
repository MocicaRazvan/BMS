package com.mocicarazvan.rediscache.aspects;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisChildCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Aspect
//@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, ObjectMapper.class, AspectUtils.class})
public class RedisReactiveChildCacheEvictAspect extends RedisReactiveCacheEvictAspect {
    protected final RedisChildCacheUtils redisChildCacheUtils;
    protected final SimpleAsyncTaskExecutor asyncTaskExecutor;

    @Value("${spring.custom.scan.batch.size:50}")
    private int scanBatchSize;

    public RedisReactiveChildCacheEvictAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate, AspectUtils aspectUtils,
                                              RedisChildCacheUtils redisChildCacheUtils, ReverseKeysLocalCache reverseKeysLocalCache, LocalReactiveCache localReactiveCache, SimpleAsyncTaskExecutor asyncTaskExecutor) {
        super(reactiveRedisTemplate, aspectUtils, redisChildCacheUtils, localReactiveCache, reverseKeysLocalCache, asyncTaskExecutor);
        this.redisChildCacheUtils = redisChildCacheUtils;
        this.asyncTaskExecutor = asyncTaskExecutor;
    }

    @Around("execution(* *(..)) && @annotation(com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict)")
    public Object redisReactiveCacheChildEvict(ProceedingJoinPoint joinPoint) {
        Method method = aspectUtils.getMethod(joinPoint);
        Class<?> returnType = method.getReturnType();
        RedisReactiveChildCacheEvict annotation = method.getAnnotation(RedisReactiveChildCacheEvict.class);
        String key = aspectUtils.extractKeyFromAnnotation(annotation.key(), joinPoint);
        String idSpel = annotation.id();
        String masterIdSpel = annotation.masterId();
        Long masterId;
        String masterPath = annotation.masterPath();
        if (masterIdSpel != null && !masterIdSpel.isBlank()) {
            masterId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(masterIdSpel, joinPoint), -1L);
        } else {
            masterId = null;
        }
        Long annId;
        if (idSpel != null && !idSpel.isBlank()) {
            annId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(idSpel, joinPoint));
        } else {
            annId = null;
        }

        if (annId == null && masterId == null && (masterPath == null || masterPath.isBlank())) {
            //cant actually happen
            throw new RuntimeException("RedisReactiveCacheChildEvict: Annotated method has invalid arguments, expected at least one of id, masterId, masterPath to be present");
        }

        if (returnType.isAssignableFrom(Mono.class)) {

            if (masterPath != null && !(masterPath.isBlank())) {
                return Mono.defer(() -> methodMonoResponseToCacheByMasterPath(joinPoint, key, annId, masterPath));
            }


            return Mono.defer(() -> invalidateForChild(key, annId, masterId))
                    .then(methodResponse(joinPoint));
        }

        throw new RuntimeException("RedisReactiveCacheChildEvict: Annotated method has invalid return type, expected return type to be Mono<?>");

    }

    @SuppressWarnings("unchecked")
    protected Mono<Object> methodMonoResponseToCacheByMasterPath(ProceedingJoinPoint joinPoint, String key, Long annId, String masterPath) {
        try {
            return ((Mono<Object>) joinPoint.proceed(joinPoint.getArgs()))
                    .flatMap(methodResponse -> invalidateForChildMasterPath(joinPoint, key, annId, masterPath, methodResponse)
//                            .doOnNext(t -> log.info("METHOD RESPONSE: " + methodResponse))
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
        Mono<Long> zipReverse;
        String reverseKey = redisChildCacheUtils.createReverseIndexKey(key, id);
        if (id != null) {
            keysFromReverse = reactiveRedisTemplate.opsForSet().members(reverseKey).cast(String.class);
            zipReverse = reactiveRedisTemplate.delete(reverseKey);
        } else {
            zipReverse = Mono.empty();
        }


        return Flux.concat(keysFromReverse,
                        keysToInvalidateByMaster(key, masterId))
                .collect(Collectors.toSet())
                .flatMap(mainKeys ->
                        redisChildCacheUtils.deleteListFromRedis(mainKeys)
                                .zipWith(zipReverse.defaultIfEmpty(0L))
                                .map(t -> t.getT1() + t.getT2())
                                .doOnSuccess(_ -> invalidateForIdLocalPrefix(key, id, mainKeys))
                );


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

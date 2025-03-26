package com.mocicarazvan.rediscache.aspects;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisChildCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

@Slf4j
@Aspect
//@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, ObjectMapper.class, AspectUtils.class})
public class RedisReactiveCacheChildAspect extends RedisReactiveCacheAspect {
    private final RedisChildCacheUtils redisChildCacheUtils;

    public RedisReactiveCacheChildAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate, AspectUtils aspectUtils, ObjectMapper objectMapper,
                                         @Qualifier("redisAsyncTaskExecutor") SimpleAsyncTaskExecutor asyncTaskExecutor, RedisChildCacheUtils redisChildCacheUtil,
                                         ReverseKeysLocalCache reverseKeysLocalCache, LocalReactiveCache localReactiveCache
    ) {
        super(reactiveRedisTemplate, aspectUtils, objectMapper, asyncTaskExecutor, redisChildCacheUtil, reverseKeysLocalCache, localReactiveCache);
        this.redisChildCacheUtils = redisChildCacheUtil;
    }

    @Around("execution(* *(..)) && @annotation(com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache)")
    public Object redisReactiveCacheChildAdd(ProceedingJoinPoint joinPoint) {
        Method method = aspectUtils.getMethod(joinPoint);
        Class<?> returnType = method.getReturnType();
        RedisReactiveChildCache annotation = method.getAnnotation(RedisReactiveChildCache.class);
        String key = aspectUtils.extractKeyFromAnnotation(annotation.key(), joinPoint);
        String idSpel = annotation.id();
        String argsHash = aspectUtils.getHashString(joinPoint, key, method.getName());
        String idPath = annotation.idPath();
        boolean saveToCache = annotation.saveToCache();
        String masterIdSpel = annotation.masterId();
        Long masterId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(masterIdSpel, joinPoint), -1L);
        if (returnType.isAssignableFrom(Mono.class)) {
            redisChildCacheUtils.checkValidId(idSpel);
            Long annId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(idSpel, joinPoint));
            // master first for efficient retrieval
            String savingKey = redisChildCacheUtils.getMasterKey(key, masterId) + redisChildCacheUtils.getSingleKey("child", annId) + redisChildCacheUtils.getHashKey(argsHash);
            return Mono.defer(() -> createBaseMono(savingKey, method))
                    .switchIfEmpty(Mono.defer(() -> methodMonoResponseToCache(joinPoint, key, savingKey, annId, saveToCache)));

        } else if (returnType.isAssignableFrom(Flux.class)) {
            redisChildCacheUtils.checkValidId(idPath);
            String savingKey = redisChildCacheUtils.getMasterKey(key, masterId) + redisChildCacheUtils.getListKey("child") + redisChildCacheUtils.getHashKey(argsHash);
            return Flux.defer(() -> createBaseFlux(savingKey, method))
                    .switchIfEmpty(Flux.defer(() -> methodFluxResponseToCache(joinPoint, key, savingKey, idPath, saveToCache)));

        }
        throw new RuntimeException("redisReactiveCacheChildAdd: Annotated method has invalid return type, expected return type to be Mono<?> or Flux<?>");
    }
}

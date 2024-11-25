package com.mocicarazvan.rediscache.aspects;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCache;
import com.mocicarazvan.rediscache.enums.BooleanEnum;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisApprovedCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

@Slf4j
@Aspect
//@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, ObjectMapper.class, AspectUtils.class})

public class RedisReactiveCacheApprovedAspect extends RedisReactiveCacheAspect {
    private final RedisApprovedCacheUtils redisApprovedCacheUtils;

    public RedisReactiveCacheApprovedAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate, AspectUtils aspectUtils, ObjectMapper objectMapper, ExecutorService executorService, RedisApprovedCacheUtils redisApprovedCacheUtils) {
        super(reactiveRedisTemplate, aspectUtils, objectMapper, executorService, redisApprovedCacheUtils);
        this.redisApprovedCacheUtils = redisApprovedCacheUtils;
    }

    @Around("execution(* *(..)) && @annotation(com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCache)")
    public Object redisReactiveCacheApprovedAdd(ProceedingJoinPoint joinPoint) {
        Method method = aspectUtils.getMethod(joinPoint);
        Class<?> returnType = method.getReturnType();
        RedisReactiveApprovedCache annotation = method.getAnnotation(RedisReactiveApprovedCache.class);

        String key = aspectUtils.extractKeyFromAnnotation(annotation.key(), joinPoint);
        String idSpel = annotation.id();
        String argsHash = aspectUtils.getHashString(joinPoint, key, method.getName());
        String idPath = annotation.idPath();
        String forWhom = annotation.forWhom();
        boolean saveToCache = annotation.saveToCache();
        if (returnType.isAssignableFrom(Mono.class)) {
            redisApprovedCacheUtils.checkValidId(idSpel);
            Long annId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(idSpel, joinPoint));
            String savingKey = redisApprovedCacheUtils.getSingleKey(key, annId) + redisApprovedCacheUtils.getHashKey(argsHash);
            return createBaseMono(savingKey, method)
                    .switchIfEmpty(methodMonoResponseToCache(joinPoint, key, savingKey, annId, saveToCache));

        } else if (returnType.isAssignableFrom(Flux.class)) {
            BooleanEnum approved = redisApprovedCacheUtils.getApprovedArg(joinPoint, annotation);
            redisApprovedCacheUtils.checkValidId(idPath);
            redisApprovedCacheUtils.checkValidId(forWhom);
            Long forWhomId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(forWhom, joinPoint));
            String savingKey = redisApprovedCacheUtils.getListKey(key) + redisApprovedCacheUtils.getApprovedKey(approved) +
                    redisApprovedCacheUtils.getForWhomKey(forWhomId) + redisApprovedCacheUtils.getHashKey(argsHash);
            return createBaseFlux(savingKey, method)
                    .switchIfEmpty(methodFluxResponseToCache(joinPoint, key, savingKey, idPath, saveToCache));

        }
        throw new RuntimeException("redisReactiveCacheApprovedAdd: Annotated method has invalid return type, expected return type to be Mono<?> or Flux<?>");
    }


}

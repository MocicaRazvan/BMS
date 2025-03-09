package com.mocicarazvan.userservice.cache.redis.asepects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.aspects.RedisReactiveCacheAspect;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.userservice.cache.redis.annotations.RedisReactiveRoleCache;
import com.mocicarazvan.userservice.cache.redis.enums.RoleAnn;
import com.mocicarazvan.userservice.cache.redis.utils.RedisRoleCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, ObjectMapper.class, AspectUtils.class})
public class RedisReactiveRoleCacheAspect extends RedisReactiveCacheAspect {
    private final RedisRoleCacheUtils redisRoleCacheUtils;

    public RedisReactiveRoleCacheAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate, AspectUtils aspectUtils, ObjectMapper objectMapper, @Qualifier("redisAsyncTaskExecutor") SimpleAsyncTaskExecutor asyncTaskExecutor, RedisRoleCacheUtils redisRoleCacheUtils, ReverseKeysLocalCache reverseKeysLocalCache, LocalReactiveCache localReactiveCache) {
        super(reactiveRedisTemplate, aspectUtils, objectMapper, asyncTaskExecutor, redisRoleCacheUtils, reverseKeysLocalCache, localReactiveCache);
        this.redisRoleCacheUtils = redisRoleCacheUtils;
    }

    @Around("execution(* *(..)) && @annotation(com.mocicarazvan.userservice.cache.redis.annotations.RedisReactiveRoleCache)")
    public Object redisReactiveCacheRoleAdd(ProceedingJoinPoint joinPoint) {
        Method method = aspectUtils.getMethod(joinPoint);
        Class<?> returnType = method.getReturnType();
        RedisReactiveRoleCache annotation = method.getAnnotation(RedisReactiveRoleCache.class);
        RoleAnn roleAnn = getRoleAnnArg(joinPoint, annotation);
        String key = aspectUtils.extractKeyFromAnnotation(annotation.key(), joinPoint);
        String idSpel = annotation.id();
        String argsHash = aspectUtils.getHashString(joinPoint, key, method.getName());
        String idPath = annotation.idPath();
        boolean saveToCache = annotation.saveToCache();
        if (returnType.isAssignableFrom(Mono.class)) {
            redisCacheUtils.checkValidId(idSpel);
            Long annId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(idSpel, joinPoint));
            String savingKey = redisCacheUtils.getSingleKey(key, annId) + redisCacheUtils.getHashKey(argsHash);
            return createBaseMono(savingKey, method)
                    .switchIfEmpty(methodMonoResponseToCache(joinPoint, key, savingKey, annId, saveToCache));

        } else if (returnType.isAssignableFrom(Flux.class)) {
            redisCacheUtils.checkValidId(idPath);
            String savingKey = redisCacheUtils.getListKey(key) + redisRoleCacheUtils.getRoleKey(roleAnn) + redisCacheUtils.getHashKey(argsHash);
            return createBaseFlux(savingKey, method)
                    .switchIfEmpty(methodFluxResponseToCache(joinPoint, key, savingKey, idPath, saveToCache));

        }
        throw new RuntimeException("redisReactiveCacheRoleAdd: Annotated method has invalid return type, expected return type to be Mono<?> or Flux<?>");
    }

    public RoleAnn getRoleAnnArg(ProceedingJoinPoint joinPoint, RedisReactiveRoleCache annotation) {
        if (annotation.roleArgumentPath() == null || annotation.roleArgumentPath().isBlank()) {
            return RoleAnn.fromRole(annotation.role());
        }
        Role role = redisRoleCacheUtils.assertRole(aspectUtils.evaluateSpelExpression(annotation.roleArgumentPath(), joinPoint));

        return RoleAnn.fromRole(role);
    }


}

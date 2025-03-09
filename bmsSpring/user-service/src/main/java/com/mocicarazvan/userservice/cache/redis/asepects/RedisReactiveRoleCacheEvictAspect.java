package com.mocicarazvan.userservice.cache.redis.asepects;

import com.mocicarazvan.rediscache.aspects.RedisReactiveCacheEvictAspect;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisApprovedCacheUtils;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.userservice.cache.redis.annotations.RedisReactiveRoleCacheEvict;
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
import reactor.util.function.Tuple3;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Aspect
@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, AspectUtils.class, RedisApprovedCacheUtils.class})
public class RedisReactiveRoleCacheEvictAspect extends RedisReactiveCacheEvictAspect {
    private final SimpleAsyncTaskExecutor asyncTaskExecutor;
    private final RedisRoleCacheUtils redisRoleCacheUtils;

    public RedisReactiveRoleCacheEvictAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                             AspectUtils aspectUtils, RedisRoleCacheUtils redisRoleCacheUtils,
                                             @Qualifier("redisAsyncTaskExecutor") SimpleAsyncTaskExecutor asyncTaskExecutor, ReverseKeysLocalCache reverseKeysLocalCache, LocalReactiveCache localReactiveCache) {
        super(reactiveRedisTemplate, aspectUtils, redisRoleCacheUtils, localReactiveCache, reverseKeysLocalCache, asyncTaskExecutor);
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.redisRoleCacheUtils = redisRoleCacheUtils;
    }

    @Around("execution(* *(..)) && @annotation(com.mocicarazvan.userservice.cache.redis.annotations.RedisReactiveRoleCacheEvict)")
    public Object redisReactiveCacheApprovedEvict(ProceedingJoinPoint joinPoint) {
        Method method = aspectUtils.getMethod(joinPoint);
        Class<?> returnType = method.getReturnType();
        RedisReactiveRoleCacheEvict annotation = method.getAnnotation(RedisReactiveRoleCacheEvict.class);
        String key = aspectUtils.extractKeyFromAnnotation(annotation.key(), joinPoint);
        String idSpel = annotation.id();
//        Long annId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(idSpel, joinPoint));
        RoleAnn oldRole = RoleAnn.fromRole(annotation.oldRole());
        RoleAnn newRole = RoleAnn.fromRole(annotation.newRole());
        String oldRolePath = annotation.oldRolePath();
        if (!returnType.isAssignableFrom(Mono.class)) {
            throw new RuntimeException("redisReactiveCacheRoleEvict: Annotated method has invalid return type, expected return type to be Mono<?>");
        }

        if (oldRolePath.isBlank()) {
            return invalidateByRoles(joinPoint, key, idSpel, newRole, oldRole)
                    .then(methodResponse(joinPoint));
        }

        aspectUtils.validateReturnTypeIsMonoPairClass(method, Role.class);

        return methodMonoResponseToCacheInvalidateByRoles(joinPoint, key, idSpel, newRole, oldRolePath);

    }

    protected Mono<?> methodMonoResponseToCacheInvalidateByRoles(ProceedingJoinPoint joinPoint, String key, String idSpel, RoleAnn newRole, String oldRolePath) {
        try {
            return ((Mono<?>) joinPoint.proceed(joinPoint.getArgs()))
                    .doOnNext(methodResponse -> {
                        asyncTaskExecutor.submit(() -> invalidateMonoByRoles(joinPoint, key, idSpel, newRole, oldRolePath, methodResponse));

                    });

        } catch (Throwable e) {
            return Mono.error(e);
        }
    }

    private void invalidateMonoByRoles(ProceedingJoinPoint joinPoint, String key, String idSpel, RoleAnn newRole, String oldRolePath, Object methodResponse) {

        RoleAnn oldRole = RoleAnn.fromRole(redisRoleCacheUtils.assertRole(aspectUtils.evaluateSpelExpressionForObject(oldRolePath, methodResponse, joinPoint)));

        invalidateByRoles(joinPoint, key, idSpel, newRole, oldRole)
                .subscribe(success ->
                        {
//                            log.info("Invalidated key: " + key + " for id: " + idSpel + " with success: " + success);
                        }
                );
    }


    protected Mono<Long> invalidateByRoles(ProceedingJoinPoint joinPoint, String key, String idSpel, RoleAnn newRole, RoleAnn oldRole) {
        Tuple3<Flux<String>, Mono<Long>, Long> result = redisRoleCacheUtils.getOptionalIdDelete(joinPoint, key, idSpel);

        return
                Flux.concat(result.getT1(),
                                keysToInvalidateRole(key, newRole, oldRole)).collectList()
                        .flatMap(redisKeys ->
//                                reactiveRedisTemplate.delete(redisKeys.toArray(String[]::new)
//                                        ).defaultIfEmpty(0L)
                                        redisRoleCacheUtils.deleteListFromRedis(redisKeys)
                                                .doOnSuccess(_ -> invalidateForIdLocalPrefix(key, result.getT3(), redisKeys))
                                                .zipWith(result.getT2().defaultIfEmpty(0L))
                                                .map(t -> t.getT1() + t.getT2())
                        );
    }


    protected Flux<String> keysToInvalidateRole(String key, RoleAnn newRole, RoleAnn oldRole) {
        List<String> patterns = new ArrayList<>();
        patterns.add(redisCacheUtils.getListKey(key) + redisRoleCacheUtils.getRoleKey(RoleAnn.NULL_ANN) + "*");
        patterns.add(redisCacheUtils.getListKey(key) + redisRoleCacheUtils.getRoleKey(newRole) + "*");
        if (!newRole.equals(oldRole) && !RoleAnn.NULL_ANN.equals(oldRole)) {
            patterns.add(redisCacheUtils.getListKey(key) + redisRoleCacheUtils.getRoleKey(oldRole) + "*");
        }
//        log.info("keysToInvalidateRole to invalidate: " + patterns);
        return redisRoleCacheUtils.getActualKeys(patterns, reactiveRedisTemplate);
    }

}

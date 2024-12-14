package com.mocicarazvan.rediscache.aspects;


import com.mocicarazvan.rediscache.annotation.RedisReactiveCacheEvict;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

@Slf4j
@Aspect
//@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, AspectUtils.class, RedisCacheUtils.class})
@RequiredArgsConstructor
public class RedisReactiveCacheEvictAspect {
    protected final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    protected final AspectUtils aspectUtils;
    protected final RedisCacheUtils redisCacheUtils;


    @Around("execution(* *(..)) && @annotation(com.mocicarazvan.rediscache.annotation.RedisReactiveCacheEvict)")
    public Object redisReactiveCacheEvict(ProceedingJoinPoint joinPoint) {
        Method method = aspectUtils.getMethod(joinPoint);
        Class<?> returnType = method.getReturnType();
        RedisReactiveCacheEvict annotation = method.getAnnotation(RedisReactiveCacheEvict.class);
        String key = aspectUtils.extractKeyFromAnnotation(annotation.key(), joinPoint);
        String idSpel = annotation.id();
        if (returnType.isAssignableFrom(Mono.class)) {
            redisCacheUtils.checkValidId(idSpel);
            Long annId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpression(idSpel, joinPoint));

            return invalidateForId(key, annId)
                    .then(methodResponse(joinPoint));
        }

        throw new RuntimeException("RedisReactiveCacheEvict: Annotated method has invalid return type, expected return type to be Mono<?>");
    }

    protected Mono<Object> methodResponse(ProceedingJoinPoint joinPoint) {
        try {
            return ((Mono<Object>) joinPoint.proceed(joinPoint.getArgs()))
//                    .doOnNext(el -> log.info("METHOD RESPONSE: " + el))
                    ;
        } catch (Throwable throwable) {
            return Mono.error(throwable);
        }
    }

    protected Mono<Long> invalidateForId(String key, Long id) {
        return reactiveRedisTemplate.delete(reactiveRedisTemplate.opsForSet().members(redisCacheUtils.createReverseIndexKey(key, id)).cast(String.class))
                .defaultIfEmpty(0L)
                .zipWith(reactiveRedisTemplate.delete(redisCacheUtils.createReverseIndexKey(key, id)).defaultIfEmpty(0L))
                .doOnNext(success -> log.info("Invalidated key: " + key + " for id: " + id))
                .map(t -> t.getT1() + t.getT2());
    }
}

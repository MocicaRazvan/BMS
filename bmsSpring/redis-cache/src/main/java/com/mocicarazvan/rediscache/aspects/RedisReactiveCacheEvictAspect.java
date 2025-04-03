package com.mocicarazvan.rediscache.aspects;


import com.mocicarazvan.rediscache.annotation.RedisReactiveCacheEvict;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Aspect
//@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, AspectUtils.class, RedisCacheUtils.class})
@RequiredArgsConstructor
public class RedisReactiveCacheEvictAspect {
    protected final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    protected final AspectUtils aspectUtils;
    protected final RedisCacheUtils redisCacheUtils;
    protected final LocalReactiveCache localReactiveCache;
    protected final ReverseKeysLocalCache reverseKeysLocalCache;
    protected final SimpleAsyncTaskExecutor asyncTaskExecutor;


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

            return Mono.defer(() -> invalidateForId(key, annId))
                    .then(methodResponse(joinPoint));
        }

        throw new RuntimeException("RedisReactiveCacheEvict: Annotated method has unsupported return type, expected Mono<?> or Flux<?>");
    }

    @SuppressWarnings("unchecked")
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
        String reverseKey = redisCacheUtils.createReverseIndexKey(key, id);
        return reactiveRedisTemplate.opsForSet().members(reverseKey)
                .cast(String.class).collectList().flatMap(reverseKeys ->
//                        reactiveRedisTemplate.delete(reverseKeys.toArray(String[]::new))
//                                .defaultIfEmpty(0L)
                                redisCacheUtils.deleteListFromRedis(reverseKeys)
                                        .zipWith(reactiveRedisTemplate.delete(reverseKey)
                                                .defaultIfEmpty(0L))
//                                        .doOnNext(success -> log.info("Invalidated key: " + key + " for id: " + id))
                                        .map(t -> t.getT1() + t.getT2())
                                        .doOnSuccess(_ -> invalidateForIdLocal(key, id, reverseKeys))
                );
    }

    protected void invalidateForIdLocal(String key, Long id, Collection<String> redisKeys) {
        invalidateForIdGeneric(key, id, redisKeys, localReactiveCache::removeNotify);
    }

    protected void invalidateForIdLocalPrefix(String key, Long id, Collection<String> redisKeys) {
        invalidateForIdGeneric(key, id, redisKeys, localReactiveCache::removeByPrefixNotify);
    }

    protected void invalidateForIdGeneric(String key, Long id, Collection<String> redisKeys,
                                          Consumer<Set<String>> consumer
    ) {
        asyncTaskExecutor.submit(() -> {
            Set<String> allKeys = new HashSet<>(redisKeys);
            if (id != null) {
                String reverseKey = redisCacheUtils.createReverseIndexKey(key, id);
                allKeys.addAll(reverseKeysLocalCache.get(reverseKey));
                reverseKeysLocalCache.removeNotify(reverseKey);
            }
            consumer.accept(allKeys);
        });
    }

}

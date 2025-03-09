package com.mocicarazvan.rediscache.aspects;


import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCacheEvict;
import com.mocicarazvan.rediscache.enums.BooleanEnum;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisApprovedCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Aspect
//@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, AspectUtils.class, RedisApprovedCacheUtils.class})
public class RedisReactiveCacheApprovedEvictAspect extends RedisReactiveCacheEvictAspect {

    protected final RedisApprovedCacheUtils redisApprovedCacheUtils;

    public RedisReactiveCacheApprovedEvictAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate, AspectUtils aspectUtils, RedisApprovedCacheUtils redisApprovedCacheUtils
            , ReverseKeysLocalCache reverseKeysLocalCache, LocalReactiveCache localReactiveCache, SimpleAsyncTaskExecutor asyncTaskExecutor
    ) {
        super(reactiveRedisTemplate, aspectUtils, redisApprovedCacheUtils, localReactiveCache, reverseKeysLocalCache, asyncTaskExecutor);
        this.redisApprovedCacheUtils = redisApprovedCacheUtils;
    }


    @Around("execution(* *(..)) && @annotation(com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCacheEvict)")
    public Object redisReactiveCacheApprovedEvict(ProceedingJoinPoint joinPoint) {
        Method method = aspectUtils.getMethod(joinPoint);
        RedisReactiveApprovedCacheEvict annotation = method.getAnnotation(RedisReactiveApprovedCacheEvict.class);

        String key = aspectUtils.extractKeyFromAnnotation(annotation.key(), joinPoint);
        String idSpel = annotation.id();
        String forWhomPath = annotation.forWhomPath();
        aspectUtils.validateReturnTypeIsMonoPairClass(method, Boolean.class);

        return methodMonoResponseToCacheInvalidateApproved(joinPoint, key, idSpel, forWhomPath);

    }

    @SuppressWarnings("unchecked")
    protected Mono<Object> methodMonoResponseToCacheInvalidateApproved(ProceedingJoinPoint joinPoint, String key, String idSpel, String forWhomPath) {
        try {
            return ((Mono<Object>) joinPoint.proceed(joinPoint.getArgs()))
                    .flatMap(el -> {
                        Pair<Object, Boolean> pair = (Pair<Object, Boolean>) el;
                        return invalidateForByIdAndOriginalApproved(joinPoint, key, idSpel, forWhomPath,
                                pair.getFirst(),
                                BooleanEnum.fromBoolean(pair.getSecond()))
                                .thenReturn(el);

                    });

        } catch (Throwable e) {
            return Mono.error(e);
        }
    }

    protected Mono<Long> invalidateForByIdAndOriginalApproved(ProceedingJoinPoint joinPoint, String key, String idSpel, String forWhomPath, Object item, BooleanEnum originalApproved) {
        Tuple3<Flux<String>, Mono<Long>, Long> result = redisApprovedCacheUtils.getOptionalIdDelete(joinPoint, key, idSpel);
        return
                Flux.concat(result.getT1(),
                                keysToInvalidateByOriginalApproved(joinPoint, item, key, forWhomPath, originalApproved)).collectList()
                        .flatMap(redisKeys ->
//                                reactiveRedisTemplate.delete(redisKeys.toArray(String[]::new))
                                redisCacheUtils.deleteListFromRedis(redisKeys)
                                        .defaultIfEmpty(0L)
                                        .doOnSuccess(_ -> invalidateForIdLocalPrefix(key, result.getT3(), redisKeys))
                                        .zipWith(result.getT2().defaultIfEmpty(0L))
                                        .map(t -> t.getT1() + t.getT2()));
    }


    protected Flux<String> keysToInvalidateByOriginalApproved(ProceedingJoinPoint joinPoint, Object item, String key, String forWhomPath, BooleanEnum originalApproved) {
        List<String> patterns = new ArrayList<>();
        Long forWhomId = aspectUtils.assertLong(aspectUtils.evaluateSpelExpressionForObject(forWhomPath, item, joinPoint));
        // invaliate for trainer
        patterns.add(createPattern(key, forWhomId, BooleanEnum.NULL));
        patterns.add(createPattern(key, forWhomId, BooleanEnum.FALSE));
        // invaldiate for admin
        patterns.add(createPattern(key, 0L, BooleanEnum.NULL));
        patterns.add(createPattern(key, 0L, BooleanEnum.FALSE));

        //this is usless for public
        patterns.add(createPattern(key, -1L, BooleanEnum.FALSE));
        patterns.add(createPattern(key, -1L, BooleanEnum.NULL));
        if (originalApproved.equals(BooleanEnum.TRUE)) {
            patterns.add(createPattern(key, forWhomId, BooleanEnum.TRUE));
            patterns.add(createPattern(key, 0L, BooleanEnum.TRUE));
            patterns.add(createPattern(key, -1L, BooleanEnum.TRUE));
        }
//        log.info("keysToInvalidateByOriginalApproved to invalidate: " + patterns);
        return redisApprovedCacheUtils.getActualKeys(patterns, reactiveRedisTemplate);
    }

    private String createPattern(String key, Long forWhomId, BooleanEnum approved) {
        return redisApprovedCacheUtils.getListKey(key) + redisApprovedCacheUtils.getApprovedKey(approved) + redisApprovedCacheUtils.getForWhomKey(forWhomId) + "*";
    }


}

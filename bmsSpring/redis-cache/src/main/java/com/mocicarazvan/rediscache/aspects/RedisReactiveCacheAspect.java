package com.mocicarazvan.rediscache.aspects;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

@Slf4j
@Aspect
//@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, ObjectMapper.class, AspectUtils.class})
@RequiredArgsConstructor
public class RedisReactiveCacheAspect {

    protected final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    protected final AspectUtils aspectUtils;
    protected final ObjectMapper objectMapper;
    protected final ExecutorService executorService;
    protected final RedisCacheUtils redisCacheUtils;

    @Value("${spring.custom.cache.redis.expire.minutes:30}")
    protected Long expireMinutes;

    @Around("execution(* *(..)) && @annotation(com.mocicarazvan.rediscache.annotation.RedisReactiveCache)")
    public Object redisReactiveCacheAdd(ProceedingJoinPoint joinPoint) {
        Method method = aspectUtils.getMethod(joinPoint);
        Class<?> returnType = method.getReturnType();
        RedisReactiveCache annotation = method.getAnnotation(RedisReactiveCache.class);
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
            String savingKey = redisCacheUtils.getListKey(key) + redisCacheUtils.getHashKey(argsHash);
            return createBaseFlux(savingKey, method)
                    .switchIfEmpty(methodFluxResponseToCache(joinPoint, key, savingKey, idPath, saveToCache));

        }
        throw new RuntimeException("RedisReactiveCacheUpdate: Annotated method has unsupported return type, expected Mono<?> or Flux<?>");


    }

    protected Flux<Object> createBaseFlux(String savingKey, Method method) {
        return reactiveRedisTemplate.opsForValue().get(savingKey)
                .flatMapMany(cr -> Flux.fromIterable((List<?>) cr))
                .log()
                .map(cr -> objectMapper.convertValue(cr, aspectUtils.getTypeReference(method)))
                .cast(Object.class)
                .onErrorResume(e -> Flux.empty());
    }

    protected Mono<Object> createBaseMono(String savingKey, Method method) {
        return reactiveRedisTemplate.opsForValue()
                .get(savingKey)
                .map(cr -> objectMapper.convertValue(cr, aspectUtils.getTypeReference(method)))
                .cast(Object.class)
                .onErrorResume(e -> Mono.empty());
    }


    protected Mono<?> methodMonoResponseToCache(ProceedingJoinPoint joinPoint, String key, String savingKey, Long annId, boolean saveToCache) {
        try {
            return ((Mono<?>) joinPoint.proceed(joinPoint.getArgs()))
                    .doOnNext(methodResponse -> {
                        log.info("Setting key: " + key + " with value: " + methodResponse);
                        if (saveToCache) {
                            executorService.submit(() -> saveMonoResultToCache(joinPoint, key, savingKey, annId, methodResponse));
                        }
                    });
        } catch (Throwable e) {
            return Mono.error(e);
        }
    }

    protected void saveMonoResultToCache(ProceedingJoinPoint joinPoint, String key, String savingKey, Long annId, Object methodResponse) {
        saveMonoToCacheNoSubscribe(key, savingKey, annId, methodResponse)
                .subscribe(
                        success -> log.info("Key: " + savingKey + " set successfully"), // Log success
                        error -> log.error("Failed to set key: " + savingKey, error) // Log errors
                );
    }

    protected Mono<Long> saveMonoToCacheNoSubscribe(String key, String savingKey, Long annId, Object methodResponse) {
        return reactiveRedisTemplate.opsForValue()
                .set(savingKey, methodResponse, Duration.ofMinutes(expireMinutes))
                .then(addToReverseIndex(key, annId, savingKey));
    }

    protected Flux<Object> methodFluxResponseToCache(ProceedingJoinPoint joinPoint, String key, String savingKey, String idPath, boolean saveToCache) {
        try {
            ConcurrentMap<Long, Object> indexMap = new ConcurrentHashMap<>();
            return ((Flux<Object>) joinPoint.proceed(joinPoint.getArgs()))
                    .index()
                    .doOnNext(indexedValue -> {
                        log.info("Processing value: index={}, value={}", indexedValue.getT1(), indexedValue.getT2());
                        indexMap.put(indexedValue.getT1(), indexedValue.getT2());
                        log.info("Current state of indexMap: {}", indexMap);

                    })
                    .map(Tuple2::getT2)
                    .doOnComplete(() -> {
                        if (saveToCache) {
                            executorService.submit(() -> saveFluxResultToCache(joinPoint, key, savingKey, idPath, indexMap));
                        }
                    });
        } catch (Throwable e) {
            return Flux.error(e);
        }
    }

    protected void saveFluxResultToCache(ProceedingJoinPoint joinPoint, String key, String savingKey, String idPath, ConcurrentMap<Long, Object> indexMap) {
        List<Long> ids = new ArrayList<>();
        List<Object> sortedList = indexMap.entrySet()
                .stream()
                .sorted(ConcurrentMap.Entry.comparingByKey())
                .map(ConcurrentMap.Entry::getValue)
                .peek(object -> {
                    Long id = aspectUtils.assertLong(aspectUtils.evaluateSpelExpressionForObject(idPath, object, joinPoint));
                    ids.add(id);
                })
                .toList();
        log.info("Setting key: " + key + " with value: " + sortedList);

        if (sortedList.isEmpty()) {
            return;
        }


        reactiveRedisTemplate.opsForValue().set(savingKey, sortedList, Duration.ofMinutes(expireMinutes))
                .flatMapMany(s -> Flux.fromIterable(ids)
                        .flatMap(id -> addToReverseIndex(key, id, savingKey)))
                .subscribe(
                        success -> log.info("Key: " + savingKey + " set successfully"), // Log success
                        error -> log.error("Failed to set key: " + savingKey, error) // Log errors
                );
    }


    protected Mono<Long> addToReverseIndex(String key, Long id, String indexKey) {
        if (id == null) {
            return Mono.empty();
        }
        log.info("Adding to reverse index: " + key + ":" + id + " value: " + indexKey);
        String reverseIndexKey = redisCacheUtils.createReverseIndexKey(key, id);
        return
                reactiveRedisTemplate
                        .opsForSet()
                        .add(reverseIndexKey, indexKey)
                        .flatMap(v -> reactiveRedisTemplate.expire(reverseIndexKey, Duration.ofMinutes(expireMinutes + 1))
                                .thenReturn(v))
                        .doOnNext(success -> log.info("Added to reverse index: " + key + ":" + id + " value: " + indexKey))
                        .doOnError(error -> log.error("Failed to add to reverse index: " + key + ":" + id + " value: " + indexKey, error));
    }


}

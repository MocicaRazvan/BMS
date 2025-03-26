package com.mocicarazvan.rediscache.aspects;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
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

@Slf4j
@Aspect
//@Component
@ConditionalOnClass({ReactiveRedisTemplate.class, ObjectMapper.class, AspectUtils.class})
public class RedisReactiveCacheAspect {

    protected final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    protected final AspectUtils aspectUtils;
    protected final ObjectMapper objectMapper;
    protected final SimpleAsyncTaskExecutor asyncTaskExecutor;
    protected final RedisCacheUtils redisCacheUtils;
    protected final ReverseKeysLocalCache reverseKeysLocalCache;
    protected final LocalReactiveCache localReactiveCache;

    @Value("${spring.custom.cache.redis.expire.minutes:30}")
    protected Long expireMinutes;

    @Value("${spring.custom.cache.redis.local.max.size:1000}")
    protected Integer maxSize;

    public RedisReactiveCacheAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                    AspectUtils aspectUtils, ObjectMapper objectMapper,
                                    SimpleAsyncTaskExecutor asyncTaskExecutor,
                                    RedisCacheUtils redisCacheUtils,
                                    ReverseKeysLocalCache reverseKeysLocalCache, LocalReactiveCache localReactiveCache
    ) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.aspectUtils = aspectUtils;
        this.objectMapper = objectMapper;
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.redisCacheUtils = redisCacheUtils;
        this.reverseKeysLocalCache = reverseKeysLocalCache;
        this.localReactiveCache = localReactiveCache;
    }

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
            return Mono.defer(() -> createBaseMono(savingKey, method))
                    .switchIfEmpty(Mono.defer(() -> methodMonoResponseToCache(joinPoint, key, savingKey, annId, saveToCache)));


        } else if (returnType.isAssignableFrom(Flux.class)) {
            redisCacheUtils.checkValidId(idPath);
            String savingKey = redisCacheUtils.getListKey(key) + redisCacheUtils.getHashKey(argsHash);
            return Flux.defer(() -> createBaseFlux(savingKey, method))
                    .switchIfEmpty(Flux.defer(() -> methodFluxResponseToCache(joinPoint, key, savingKey, idPath, saveToCache)));

        }
        throw new RuntimeException("RedisReactiveCacheUpdate: Annotated method has unsupported return type, expected Mono<?> or Flux<?>");


    }

    @SuppressWarnings("unchecked")
    protected Flux<Object> createBaseFlux(String savingKey, Method method) {
        return
                localReactiveCache.getFluxOrEmpty(savingKey)
                        .switchIfEmpty(Flux.defer(() ->
                                reactiveRedisTemplate.opsForValue().get(savingKey)
                                        .map(collection -> (List<Object>) objectMapper.convertValue(collection, objectMapper.getTypeFactory()
                                                .constructCollectionType(List.class,
                                                        objectMapper.getTypeFactory().constructType(method.getGenericReturnType())
                                                )))
                                        .doOnNext(list -> localReactiveCache.put(savingKey, list))
                                        .flatMapMany(Flux::fromIterable)
                                        .cast(Object.class)

                        ))
                        .onErrorResume(e -> Flux.empty());
    }

    protected Mono<Object> createBaseMono(String savingKey, Method method) {
        return
                localReactiveCache.getMonoOrEmpty(savingKey)
                        .switchIfEmpty(
                                Mono.defer(() -> reactiveRedisTemplate.opsForValue()
                                        .get(savingKey)
                                        .map(cr -> objectMapper.convertValue(cr, aspectUtils.getTypeReference(method)))
                                        .cast(Object.class)
                                        .doOnSuccess(ob -> localReactiveCache.put(savingKey, ob))
                                ))
                        .onErrorResume(e -> Mono.empty());
    }


    protected Mono<?> methodMonoResponseToCache(ProceedingJoinPoint joinPoint, String key, String savingKey, Long annId, boolean saveToCache) {
        try {
            return ((Mono<?>) joinPoint.proceed(joinPoint.getArgs()))
                    .doOnNext(methodResponse -> {
//                        log.info("Setting key: " + key + " with value: " + methodResponse);
                        if (saveToCache) {
                            asyncTaskExecutor.submit(() -> saveMonoResultToCache(joinPoint, key, savingKey, annId, methodResponse));
                        }
                    });
        } catch (Throwable e) {
            return Mono.error(e);
        }
    }

    protected void saveMonoResultToCache(ProceedingJoinPoint joinPoint, String key, String savingKey, Long annId, Object methodResponse) {
        saveMonoToCacheNoSubscribe(key, savingKey, annId, methodResponse)
                .subscribe(
                        success -> {
//                            log.info("Key: " + savingKey + " set successfully");
                            localReactiveCache.put(savingKey, methodResponse);
                        }, // Log success
                        error -> log.error("Failed to set key: " + savingKey, error) // Log errors
                );
    }

    protected Mono<Long> saveMonoToCacheNoSubscribe(String key, String savingKey, Long annId, Object methodResponse) {
        return reactiveRedisTemplate.opsForValue()
                .set(savingKey, methodResponse, Duration.ofMinutes(expireMinutes))
                .then(addToReverseIndex(key, annId, savingKey));
    }

    @SuppressWarnings("unchecked")
    protected Flux<Object> methodFluxResponseToCache(ProceedingJoinPoint joinPoint, String key, String savingKey, String idPath, boolean saveToCache) {
        try {
            ConcurrentMap<Long, Object> indexMap = new ConcurrentHashMap<>();
            return ((Flux<Object>) joinPoint.proceed(joinPoint.getArgs()))
                    .index()
                    .doOnNext(indexedValue -> {
//                        log.info("Processing value: index={}, value={}", indexedValue.getT1(), indexedValue.getT2());
                        indexMap.put(indexedValue.getT1(), indexedValue.getT2());
//                        log.info("Current state of indexMap: {}", indexMap);

                    })
                    .map(Tuple2::getT2)
                    .doOnComplete(() -> {
                        if (saveToCache) {
                            asyncTaskExecutor.submit(() -> saveFluxResultToCache(joinPoint, key, savingKey, idPath, indexMap));
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
//        log.info("Setting key: " + key + " with value: " + sortedList);

        if (sortedList.isEmpty()) {
            return;
        }


        reactiveRedisTemplate.opsForValue().set(savingKey, sortedList, Duration.ofMinutes(expireMinutes))
                .flatMapMany(s -> Flux.fromIterable(ids)
                        .flatMap(id -> addToReverseIndex(key, id, savingKey)))
                .subscribe(
                        success ->
                        {
//                            log.info("Key: " + savingKey + " set successfully");
                            localReactiveCache.put(savingKey, sortedList);
                            return;
                        },// Log success
                        error -> log.error("Failed to set key: " + savingKey, error) // Log errors
                );
    }


    protected Mono<Long> addToReverseIndex(String key, Long id, String indexKey) {
        if (id == null) {
            return Mono.empty();
        }
//        log.info("Adding to reverse index: " + key + ":" + id + " value: " + indexKey);
        String reverseIndexKey = redisCacheUtils.createReverseIndexKey(key, id);
        return
                reactiveRedisTemplate
                        .opsForSet()
                        .add(reverseIndexKey, indexKey)
                        .flatMap(v -> reactiveRedisTemplate.expire(reverseIndexKey, Duration.ofMinutes(expireMinutes + 1))
                                .thenReturn(v))
                        .doOnNext(success -> {
//                            log.info("Added to reverse index: " + key + ":" + id + " value: " + indexKey);
                            return;
                        })
                        .doOnSuccess(_ -> reverseKeysLocalCache.add(reverseIndexKey, indexKey))
                        .doOnError(error -> log.error("Failed to add to reverse index: " + key + ":" + id + " value: " + indexKey, error));
    }


}

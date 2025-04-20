package com.mocicarazvan.rediscache.aspects;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisCacheUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;
import reactor.util.function.Tuples;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
    //to prevent infinite cache
    @Value("${spring.custom.max.cache.flux.seconds:3600}")
    protected Long maxCacheFluxSeconds;

    @Value("${spring.custom.cache.redis.expire.minutes:30}")
    protected Long expireMinutes;

    @Value("${spring.custom.cache.redis.flux,cache,parallelism:4}")
    protected int parallelism;

    private Scheduler scheduler;

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

    public RedisReactiveCacheAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                    AspectUtils aspectUtils, ObjectMapper objectMapper,
                                    RedisCacheUtils redisCacheUtils,
                                    ReverseKeysLocalCache reverseKeysLocalCache, LocalReactiveCache localReactiveCache
    ) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.aspectUtils = aspectUtils;
        this.objectMapper = objectMapper;
        this.asyncTaskExecutor = new SimpleAsyncTaskExecutorBuilder()
                .concurrencyLimit(4)
                .build();
        this.redisCacheUtils = redisCacheUtils;
        this.reverseKeysLocalCache = reverseKeysLocalCache;
        this.localReactiveCache = localReactiveCache;
    }

    @PostConstruct
    public void init() {
        if (this.scheduler == null) {
            this.scheduler = ForkJoinPoolScheduler.create("redis-cache-flux", parallelism);
        }

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
                                                        objectMapper.getTypeFactory().constructType(aspectUtils.getTypeReference(method)
                                                        )
                                                )))
                                        .doOnSuccess(list -> {
                                            localReactiveCache.put(savingKey, list);
                                        })
                                        .flatMapMany(Flux::fromIterable)
                                        .cast(Object.class)

                        ))
                        .onErrorResume(e -> {
                            log.error("Error while creating base flux for key: {}", savingKey, e);
                            return Flux.empty();
                        });
    }

    protected Mono<Object> createBaseMono(String savingKey, Method method) {
        return
                localReactiveCache.getMonoOrEmpty(savingKey)
                        .switchIfEmpty(
                                Mono.defer(() -> reactiveRedisTemplate.opsForValue()
                                        .get(savingKey)
                                        .map(cr -> objectMapper.convertValue(cr, aspectUtils.getTypeReference(method)))
                                        .cast(Object.class)
                                        .doOnSuccess(ob -> {
                                            localReactiveCache.put(savingKey, ob);
                                        })
                                ))
                        .onErrorResume(e -> {
                            log.error("Error while creating base mono for key: {}", savingKey, e);
                            return Mono.empty();
                        });
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
                .doOnSuccess(_ -> {
                    localReactiveCache.put(savingKey, methodResponse);
                })
                .subscribe(
                        success -> {
//                            log.info("Key: " + savingKey + " set successfully");
//                            localReactiveCache.put(savingKey, methodResponse);
                        },
                        error -> log.error("Failed to set key: {}", savingKey, error)
                );
    }

    protected Mono<Long> saveMonoToCacheNoSubscribe(String key, String savingKey, Long annId, Object methodResponse) {
        return reactiveRedisTemplate.opsForValue()
                .set(savingKey, methodResponse, Duration.ofMinutes(expireMinutes))
                .filter(Boolean::booleanValue)
                .flatMap(_ -> addToReverseIndex(key, annId, savingKey));
    }

    @SuppressWarnings("unchecked")
    protected Flux<Object> methodFluxResponseToCache(ProceedingJoinPoint joinPoint, String key, String savingKey, String idPath, boolean saveToCache) {
        try {
            Flux<Object> original = (Flux<Object>) joinPoint.proceed(joinPoint.getArgs());
            Flux<Object> cached = original.cache(Duration.ofSeconds(maxCacheFluxSeconds));
            return cached
                    .doOnComplete(() -> {
                        if (saveToCache) {
                            asyncTaskExecutor.submit(() -> saveFluxResultToCache(joinPoint, key, savingKey, idPath, cached));
                        }
                    });
        } catch (Throwable e) {
            return Flux.error(e);
        }
    }

    protected void saveFluxResultToCache(ProceedingJoinPoint joinPoint, String key, String savingKey, String idPath, Flux<Object> original) {
        // just in case
        Scheduler fallbackScheduler = scheduler != null ? scheduler : Schedulers.immediate();
        original
                .publishOn(fallbackScheduler)
                .reduce(Tuples.of(Sinks.many().unicast().<Long>onBackpressureBuffer(), new ArrayList<>()), (acc, cur) -> {
                    long id = aspectUtils.assertLong(
                            aspectUtils.evaluateSpelExpressionForObject(idPath, cur, joinPoint)
                    );
                    acc.getT1().tryEmitNext(id);
                    acc.getT2().add(cur);
                    return acc;
                })
                .doOnSuccess(t -> {
                    t.getT1().tryEmitComplete();
                })
                .flatMapMany(tuple -> {
                            Flux<Long> ids = tuple.getT1().asFlux().timeout(Duration.ofSeconds(maxCacheFluxSeconds));
                            List<Object> values = tuple.getT2();

                            return reactiveRedisTemplate.opsForValue().set(savingKey, values, Duration.ofMinutes(expireMinutes))
                                    .filter(Boolean::booleanValue)
                                    // https://github.com/spring-projects/spring-data-redis/issues/2715
                                    // no need to pipeline here
                                    .flatMapMany(_ -> ids
                                            .flatMap(id ->
                                                    addToReverseIndex(key, id, savingKey)
                                            ))
                                    .doOnComplete(() -> localReactiveCache.put(savingKey, values));
                        }
                ).subscribe(
                        success ->
                        {
//                            log.info("Key: " + savingKey + " set successfully");
                            return;
                        },
                        error -> log.error("Failed to set key: {}", savingKey, error)
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
                        .doOnError(error -> log.error("Failed to add to reverse index: {}:{} value: {}", key, id, indexKey, error));
    }


}

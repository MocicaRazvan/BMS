package com.mocicarazvan.rediscache.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.aspects.*;
import com.mocicarazvan.rediscache.configTests.TestServiceApprovedReactive;
import com.mocicarazvan.rediscache.configTests.TestServiceChildReactive;
import com.mocicarazvan.rediscache.configTests.TestServiceReactive;
import com.mocicarazvan.rediscache.impl.CacheViewServiceTestImpl;
import com.mocicarazvan.rediscache.impl.SynchronizeLocalRemoveTestImpl;
import com.mocicarazvan.rediscache.local.*;
import com.mocicarazvan.rediscache.services.CacheViewService;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisApprovedCacheUtils;
import com.mocicarazvan.rediscache.utils.RedisCacheUtils;
import com.mocicarazvan.rediscache.utils.RedisChildCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import java.util.concurrent.Executor;

@Slf4j
@TestConfiguration(proxyBeanMethods = false)
@EnableConfigurationProperties({LocalCacheProperties.class})
public class LocalCacheConfig {

    @Bean
    public SimpleAsyncTaskExecutor redisAsyncExecutorTest() {
//        return new SimpleAsyncTaskExecutorBuilder().concurrencyLimit(128).build();

        return new SimpleAsyncTaskExecutor() {
            @Override
            public void execute(Runnable task) {
                task.run();
            }
        };
    }

    @Bean
    public NotifyLocalRemove notifyLocalRemove() {
        return n -> {
            log.info("notifyLocalRemove called for dto: {}", n);
        };
    }

    @Bean
    public LocalReactiveCache localCacheTest(
            LocalCacheProperties localCacheProperties,
            Executor redisAsyncExecutorTest,
            NotifyLocalRemove notifyLocalRemove
    ) {
        return new LocalReactiveCache(
                localCacheProperties,
                redisAsyncExecutorTest,
                notifyLocalRemove
        );
    }

    @Bean
    public ReverseKeysLocalCache reverseKeysLocalCacheTest(
            LocalCacheProperties localCacheProperties,
            Executor redisAsyncExecutorTest,
            NotifyLocalRemove notifyLocalRemove
    ) {
        return new ReverseKeysLocalCache(
                localCacheProperties,
                redisAsyncExecutorTest,
                notifyLocalRemove
        );
    }

    @Bean
    public SynchronizeLocalRemove synchronizeLocalRemove(
            LocalReactiveCache localReactiveCache,
            ReverseKeysLocalCache reverseKeysLocalCache
    ) {
        return new SynchronizeLocalRemoveTestImpl(
                localReactiveCache,
                reverseKeysLocalCache
        );
    }

    public static final String CACHE_VIEW_SERVICE_ITEM = "item";
    public static final String CACHE_VIEW_SERVICE_LOCK = "item:lock";

    @Bean
    public CacheViewService cacheViewServiceTest(ReactiveStringRedisTemplate redisTemplate, FlushProperties flushProperties,
                                                 @Qualifier("redisAsyncExecutorTest") Executor executor
    ) {
        return new CacheViewServiceTestImpl(redisTemplate, CACHE_VIEW_SERVICE_ITEM, CACHE_VIEW_SERVICE_LOCK, flushProperties, executor);
    }


    @Bean
    public ObjectMapper objectMapperTest() {
        return new ObjectMapper();
    }

    @Bean
    public RedisCacheUtils redisCacheUtils(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                           AspectUtils aspectUtils) {
        return new RedisCacheUtils(aspectUtils, reactiveRedisTemplate);
    }

    @Bean
    public RedisReactiveCacheAspect redisReactiveCacheAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                             AspectUtils aspectUtils,
                                                             ObjectMapper objectMapper,
                                                             @Qualifier("redisAsyncExecutorTest") SimpleAsyncTaskExecutor asyncTaskExecutor,
                                                             RedisCacheUtils redisCacheUtils,
                                                             ReverseKeysLocalCache reverseKeysLocalCache,
                                                             LocalReactiveCache localReactiveCache) {
        return new RedisReactiveCacheAspect(reactiveRedisTemplate, aspectUtils, objectMapper, asyncTaskExecutor, redisCacheUtils, reverseKeysLocalCache, localReactiveCache);
    }

    @Bean
    public RedisReactiveCacheEvictAspect redisReactiveCacheEvictAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                                       AspectUtils aspectUtils,
                                                                       @Qualifier("redisAsyncExecutorTest") SimpleAsyncTaskExecutor asyncTaskExecutor,
                                                                       RedisCacheUtils redisCacheUtils,
                                                                       ReverseKeysLocalCache reverseKeysLocalCache,
                                                                       LocalReactiveCache localReactiveCache) {
        return new RedisReactiveCacheEvictAspect(reactiveRedisTemplate, aspectUtils, redisCacheUtils, localReactiveCache, reverseKeysLocalCache, asyncTaskExecutor);
    }

    @Bean
    public RedisChildCacheUtils redisChildCacheUtils(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                     AspectUtils aspectUtils) {
        return new RedisChildCacheUtils(aspectUtils, reactiveRedisTemplate);
    }


    @Bean
    public RedisReactiveCacheChildAspect redisReactiveCacheChildAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                                       AspectUtils aspectUtils,
                                                                       ObjectMapper objectMapper,
                                                                       @Qualifier("redisAsyncExecutorTest") SimpleAsyncTaskExecutor asyncTaskExecutor,
                                                                       RedisChildCacheUtils redisChildCacheUtils,
                                                                       ReverseKeysLocalCache reverseKeysLocalCache,
                                                                       LocalReactiveCache localReactiveCache) {
        return new RedisReactiveCacheChildAspect(reactiveRedisTemplate, aspectUtils, objectMapper, asyncTaskExecutor, redisChildCacheUtils, reverseKeysLocalCache, localReactiveCache);
    }

    @Bean
    public RedisReactiveChildCacheEvictAspect redisReactiveChildCacheEvictAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                                                 AspectUtils aspectUtils,
                                                                                 @Qualifier("redisAsyncExecutorTest") SimpleAsyncTaskExecutor asyncTaskExecutor,
                                                                                 RedisChildCacheUtils redisChildCacheUtils,
                                                                                 ReverseKeysLocalCache reverseKeysLocalCache,
                                                                                 LocalReactiveCache localReactiveCache) {
        return new RedisReactiveChildCacheEvictAspect(reactiveRedisTemplate, aspectUtils, redisChildCacheUtils, reverseKeysLocalCache, localReactiveCache, asyncTaskExecutor);
    }

    @Bean
    public RedisApprovedCacheUtils redisApprovedCacheUtils(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                           AspectUtils aspectUtils) {
        return new RedisApprovedCacheUtils(aspectUtils, reactiveRedisTemplate);
    }

    @Bean
    public RedisReactiveCacheApprovedAspect redisReactiveCacheApprovedAspect(
            ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
            AspectUtils aspectUtils,
            ObjectMapper objectMapper,
            @Qualifier("redisAsyncExecutorTest") SimpleAsyncTaskExecutor asyncTaskExecutor,
            RedisApprovedCacheUtils redisApprovedCacheUtils,
            ReverseKeysLocalCache reverseKeysLocalCache,
            LocalReactiveCache localReactiveCache
    ) {
        return new RedisReactiveCacheApprovedAspect(
                reactiveRedisTemplate,
                aspectUtils,
                objectMapper,
                asyncTaskExecutor,
                redisApprovedCacheUtils,
                reverseKeysLocalCache,
                localReactiveCache
        );
    }

    @Bean
    public RedisReactiveCacheApprovedEvictAspect reactiveCacheApprovedEvictAspect(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                                                                  AspectUtils aspectUtils,
                                                                                  @Qualifier("redisAsyncExecutorTest") SimpleAsyncTaskExecutor asyncTaskExecutor,
                                                                                  RedisApprovedCacheUtils redisApprovedCacheUtils,
                                                                                  ReverseKeysLocalCache reverseKeysLocalCache,
                                                                                  LocalReactiveCache localReactiveCache) {
        return new RedisReactiveCacheApprovedEvictAspect(reactiveRedisTemplate, aspectUtils, redisApprovedCacheUtils, reverseKeysLocalCache, localReactiveCache, asyncTaskExecutor);
    }

    @Bean
    public TestServiceReactive testServiceReactive() {
        return new TestServiceReactive();
    }

    @Bean
    public TestServiceChildReactive testServiceChildReactive() {
        return new TestServiceChildReactive();
    }

    @Bean
    public TestServiceApprovedReactive testServiceApprovedReactive() {
        return new TestServiceApprovedReactive();
    }
}

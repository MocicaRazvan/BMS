package com.mocicarazvan.rediscache.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.local.LocalCacheProperties;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.NotifyLocalRemove;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.Executor;

@Configuration
public class BeanConfigRedisCache {

    @Value("${spring.custom.cache.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.custom.cache.redis.port:6379}")
    private int redisPort;

    @Value("${spring.custom.cache.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.custom.executor.redis.async.concurrency.limit:128}")
    private int executorAsyncConcurrencyLimit;

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(redisDatabase);
        return new LettuceConnectionFactory(config);
    }

    @Bean
//    @Primary
    @ConditionalOnBean(ObjectMapper.class)
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
            ObjectMapper objectMapper
    ) {
        Jackson2JsonRedisSerializer<Object> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext(keySerializer)
                .key(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
    }

    @Bean
    public AspectUtils aspectUtils() {
        return new AspectUtils();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisAsyncTaskExecutor")
    public SimpleAsyncTaskExecutor redisAsyncTaskExecutor() {
        return new SimpleAsyncTaskExecutorBuilder()
                .virtualThreads(true)
                .threadNamePrefix("SimpleAsyncTaskRedisExecutorInstance-")
                .concurrencyLimit(executorAsyncConcurrencyLimit)
                .build();
    }

    @Bean
    @ConditionalOnBean(NotifyLocalRemove.class)
    public LocalReactiveCache localReactiveCache(LocalCacheProperties localCacheProperties,
                                                 @Qualifier("redisAsyncTaskExecutor") Executor executor,
                                                 NotifyLocalRemove notifyLocalRemove) {
        return new LocalReactiveCache(localCacheProperties, executor, notifyLocalRemove);
    }

    @Bean
    @ConditionalOnBean(NotifyLocalRemove.class)
    public ReverseKeysLocalCache reverseKeysLocalCache(LocalCacheProperties localCacheProperties,
                                                       @Qualifier("redisAsyncTaskExecutor") Executor executor, NotifyLocalRemove notifyLocalRemove) {
        return new ReverseKeysLocalCache(localCacheProperties, executor, notifyLocalRemove);
    }

}


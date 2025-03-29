package com.mocicarazvan.rediscache.config;


import com.mocicarazvan.rediscache.local.LocalCacheProperties;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.NotifyLocalRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableConfigurationProperties(LocalCacheProperties.class)
public class LocalCacheConfig {

    @Bean
    public Executor redisAsyncExecutor() {
        return new SimpleAsyncTaskExecutorBuilder().concurrencyLimit(128).build();
    }

    @Bean
    public NotifyLocalRemove notifyLocalRemove() {
        return n -> {
            log.info("notifyLocalRemove called for dto: {}", n);
        };
    }

    @Bean
    public LocalReactiveCache localCache(
            LocalCacheProperties localCacheProperties,
            Executor redisAsyncExecutor,
            NotifyLocalRemove notifyLocalRemove
    ) {
        return new LocalReactiveCache(
                localCacheProperties,
                redisAsyncExecutor,
                notifyLocalRemove
        );
    }
}

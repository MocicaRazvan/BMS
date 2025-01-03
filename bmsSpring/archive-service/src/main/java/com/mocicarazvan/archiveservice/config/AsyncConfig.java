package com.mocicarazvan.archiveservice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

import java.time.Duration;


@Configuration
public class AsyncConfig {
    @Value("${spring.custom.thread.pool.size:64}")
    private int threadPoolSize;

    @Value("${spring.custom.executor.async.concurrency.limit:128}")
    private int executorAsyncConcurrencyLimit;


    @Bean
    public SimpleAsyncTaskScheduler containerSimpleAsyncTaskScheduler() {
        return getSimpleAsyncTaskScheduler("ContainerSimpleAsyncTaskScheduler");
    }

    @Bean
    public SimpleAsyncTaskScheduler containerLifecycleSimpleAsyncTaskScheduler() {
        return getSimpleAsyncTaskScheduler("ContainerLifecycleSimpleAsyncTaskScheduler");
    }

    @Bean
    public SimpleAsyncTaskScheduler redisSimpleAsyncTaskScheduler() {
        return getSimpleAsyncTaskScheduler("RedisSimpleAsyncTaskScheduler");
    }

    public SimpleAsyncTaskScheduler getSimpleAsyncTaskScheduler(String prefix) {
        return new SimpleAsyncTaskSchedulerBuilder()
                .virtualThreads(true)
                .threadNamePrefix("SimpleAsyncTaskSchedulerInstance" + prefix + "-")
                .concurrencyLimit(threadPoolSize)
                .taskTerminationTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Bean
    public SimpleAsyncTaskExecutor simpleAsyncTaskExecutor() {
        return new SimpleAsyncTaskExecutorBuilder()
                .virtualThreads(true)
                .threadNamePrefix("SimpleAsyncTaskExecutorInstance-")
                .concurrencyLimit(executorAsyncConcurrencyLimit)
                .taskTerminationTimeout(Duration.ofSeconds(20))
                .build();
    }

}

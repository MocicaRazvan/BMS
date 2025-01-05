package com.mocicarazvan.websocketservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

import java.time.Duration;

@Configuration
public class AsyncConfig {

    @Value("${spring.custom.thread.pool.size:8}")
    private int threadPoolSize;
    @Value("${spring.custom.executor.async.concurrency.limit:64}")
    private int executorAsyncConcurrencyLimit;


    @Bean("scheduledExecutorService")
    @Primary
    public SimpleAsyncTaskExecutor scheduledExecutorService() {
        return new SimpleAsyncTaskExecutorBuilder()
                .virtualThreads(true)
                .threadNamePrefix("WSScheduledExecutor-")
                .concurrencyLimit(executorAsyncConcurrencyLimit)
                .taskTerminationTimeout(Duration.ofSeconds(15))
                .build();
    }

//    @Bean
//    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
//        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
//        threadPoolTaskScheduler.setPoolSize(threadPoolSize);
//        threadPoolTaskScheduler.setThreadNamePrefix("WSScheduler-");
//        threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
//        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
//        threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
//        return threadPoolTaskScheduler;
//
//    }
//
//    @Bean
//    public ThreadPoolTaskScheduler threadPoolTaskSchedulerVirtual() {
//        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
//        threadPoolTaskScheduler.setPoolSize(executorAsyncConcurrencyLimit);
//        threadPoolTaskScheduler.setThreadNamePrefix("WSSchedulerVirtual-");
//        threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
//        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
//        threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
//        threadPoolTaskScheduler.setThreadFactory(Thread.ofVirtual().factory());
//        return threadPoolTaskScheduler;
//
//    }

    @Bean
    public SimpleAsyncTaskScheduler simpleAsyncTaskScheduler() {
        return new SimpleAsyncTaskSchedulerBuilder()
                .virtualThreads(true)
                .threadNamePrefix("SimpleAsyncTaskSchedulerInstance-")
                .concurrencyLimit(executorAsyncConcurrencyLimit)
                .taskTerminationTimeout(Duration.ofSeconds(15))
                .build();
    }
}
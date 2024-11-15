package com.mocicarazvan.websocketservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class AsyncConfig {

    @Value("${spring.custom.thread.pool.size:16}")
    private int threadPoolSize;
    @Value("${spring.custom.executor.async.concurrency.limit:32}")
    private int executorAsyncConcurrencyLimit;

//    @Bean
//    @Primary
//    public Executor taskExecutor() {
////        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
////        executor.setCorePoolSize(4);
////        executor.setMaxPoolSize(8);
////        executor.setQueueCapacity(150);
////        executor.setKeepAliveSeconds(60);
////        executor.setThreadNamePrefix("WSExecutor-");
////        executor.initialize();
////        return executor;
//
//        ThreadFactory virtualThreadFactory = Thread.ofVirtual()
//                .name("WSExecutor-", 0)
//                .inheritInheritableThreadLocals(true)
//                .factory();
//
//        return Executors.newThreadPerTaskExecutor(virtualThreadFactory);
//    }
    //todo push and test locally

    @Bean("scheduledExecutorService")
    @Primary
    public SimpleAsyncTaskExecutor scheduledExecutorService() {
        return new SimpleAsyncTaskExecutorBuilder()
                .virtualThreads(true)
                .threadNamePrefix("WSScheduledExecutor-")
                .concurrencyLimit(executorAsyncConcurrencyLimit)
                .build();
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(threadPoolSize);
        threadPoolTaskScheduler.setThreadNamePrefix("WSScheduler-");
        threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
        return threadPoolTaskScheduler;

    }
}
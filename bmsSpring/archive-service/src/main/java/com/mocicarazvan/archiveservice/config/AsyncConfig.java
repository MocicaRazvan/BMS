package com.mocicarazvan.archiveservice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;


@Configuration
public class AsyncConfig {
    @Value("${spring.custom.virtual.thread.pool.size:32}")
    private int virtualThreadPoolSize;

    @Value("${spring.custom.executor.async.concurrency.limit:64}")
    private int executorAsyncConcurrencyLimit;

//    @Value("${spring.custom.thread.pool.size:4}")
//    private int threadPoolSize;

    @Value("${spring.custom.parallel.size:4}")
    private int parallelSize;

//    @Bean
//    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
//        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
//        threadPoolTaskScheduler.setPoolSize(threadPoolSize);
//        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolScheduler-");
//        threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
//        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
//        threadPoolTaskScheduler.setAwaitTerminationSeconds(20);
//        return threadPoolTaskScheduler;
//
//    }

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

    @Bean
    public Scheduler parallelScheduler() {
        return Schedulers.newParallel("parallel-scheduler", parallelSize);
    }

    public SimpleAsyncTaskScheduler getSimpleAsyncTaskScheduler(String prefix) {
        return new SimpleAsyncTaskSchedulerBuilder()
                .virtualThreads(true)
                .threadNamePrefix("SimpleAsyncTaskSchedulerInstance" + prefix + "-")
                .concurrencyLimit(virtualThreadPoolSize)
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

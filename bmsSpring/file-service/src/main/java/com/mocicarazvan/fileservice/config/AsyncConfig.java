package com.mocicarazvan.fileservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Configuration
public class AsyncConfig {

//    @Value("${spring.custom.thread.pool.size:16}")
//    private int threadPoolSize;

    @Value("${spring.custom.parallel.size:4}")
    private int parallelSize;

    @Value("${spring.custom.executor.async.concurrency.limit:64}")
    private int executorAsyncConcurrencyLimit;

//    @Bean
//    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
//        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
//        threadPoolTaskScheduler.setPoolSize(threadPoolSize);
//        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolScheduler-");
//        threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
//        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
//        threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
//        return threadPoolTaskScheduler;
//
//    }

    @Bean
    public Scheduler parallelScheduler() {
        return Schedulers.newParallel("parallel-scheduler", parallelSize);
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

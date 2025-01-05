package com.mocicarazvan.templatemodule.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerBuilder;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;

@Slf4j
public class SimpleTaskExecutorsInstance {


    public SimpleAsyncTaskExecutor initializeVirtual(int executorAsyncConcurrencyLimit) {
        return new SimpleAsyncTaskExecutorBuilder()
                .virtualThreads(true)
                .threadNamePrefix("SimpleAsyncTaskExecutorInstance-")
                .concurrencyLimit(executorAsyncConcurrencyLimit)
                .taskTerminationTimeout(Duration.ofSeconds(5))
                .build();
    }


    public ThreadPoolTaskScheduler initializeThreadPool(int threadPoolSize) {
        log.info("Initializing ThreadPoolTaskScheduler with size: {}", threadPoolSize);
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(threadPoolSize);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler-");
        threadPoolTaskScheduler.setRemoveOnCancelPolicy(true);
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskScheduler.setAwaitTerminationSeconds(20);
        return threadPoolTaskScheduler;
    }

    public SimpleAsyncTaskScheduler initializeVirtualScheduler(int executorAsyncConcurrencyLimit) {
        return new SimpleAsyncTaskSchedulerBuilder()
                .virtualThreads(true)
                .threadNamePrefix("SimpleAsyncTaskSchedulerInstance-")
                .concurrencyLimit(executorAsyncConcurrencyLimit)
                .taskTerminationTimeout(Duration.ofSeconds(5))
                .build();
    }
}

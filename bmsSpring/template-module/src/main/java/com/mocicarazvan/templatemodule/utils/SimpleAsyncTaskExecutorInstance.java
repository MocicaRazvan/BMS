package com.mocicarazvan.templatemodule.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.core.task.SimpleAsyncTaskExecutor;


public class SimpleAsyncTaskExecutorInstance {

    @Value("${spring.custom.executor.async.concurrency.limit:32}")
    private int executorAsyncConcurrencyLimit;


    public SimpleAsyncTaskExecutor initialize() {
        return new SimpleAsyncTaskExecutorBuilder()
                .virtualThreads(true)
                .threadNamePrefix("SimpleAsyncTaskExecutorInstance-")
                .concurrencyLimit(executorAsyncConcurrencyLimit)
                .build();
    }
}

package com.mocicarazvan.recipeservice.config;


import com.mocicarazvan.templatemodule.utils.SimpleTaskExecutorsInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class AsyncConfig {

    @Value("${spring.custom.thread.pool.size:4}")
    private int threadPoolSize;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new SimpleTaskExecutorsInstance().initializeThreadPool(threadPoolSize);

    }
}

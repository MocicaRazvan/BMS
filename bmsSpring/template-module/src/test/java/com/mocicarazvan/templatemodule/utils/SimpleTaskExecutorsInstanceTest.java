package com.mocicarazvan.templatemodule.utils;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class SimpleTaskExecutorsInstanceTest {

    private final SimpleTaskExecutorsInstance instance = new SimpleTaskExecutorsInstance();

    @Test
    void initializeVirtualThreadPool() {
        var concurrency = 128;
        var executor = instance.initializeVirtual(concurrency);
        assertNotNull(executor);
        assertEquals(concurrency, executor.getConcurrencyLimit());
        assertEquals("SimpleAsyncTaskExecutorInstance-", executor.getThreadNamePrefix());
        Thread[] threadHolder = new Thread[1];

        executor.execute(() -> {
            threadHolder[0] = Thread.currentThread();
        });

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertNotNull(threadHolder[0]);
                    assertTrue(threadHolder[0].isVirtual());
                });
    }

    @Test
    void testInitializeThreadPoolScheduler() {
        int poolSize = 10;
        ThreadPoolTaskScheduler scheduler = instance.initializeThreadPool(poolSize);

        scheduler.initialize();

        assertNotNull(scheduler);
        assertEquals("ThreadPoolTaskScheduler-", scheduler.getThreadNamePrefix());
        assertTrue(scheduler.getScheduledThreadPoolExecutor().getRemoveOnCancelPolicy());
      
        scheduler.initialize();
        scheduler.getScheduledExecutor();
    }

    @Test
    void initializeVirtualScheduler() {
        var concurrency = 128;
        var scheduler = instance.initializeVirtualScheduler(concurrency);
        assertNotNull(scheduler);
        assertEquals(concurrency, scheduler.getConcurrencyLimit());
        assertEquals("SimpleAsyncTaskSchedulerInstance-", scheduler.getThreadNamePrefix());
        Thread[] threadHolder = new Thread[1];

        scheduler.execute(() -> {
            threadHolder[0] = Thread.currentThread();
        });


        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertNotNull(threadHolder[0]);
                    assertTrue(threadHolder[0].isVirtual());
                });
    }


}
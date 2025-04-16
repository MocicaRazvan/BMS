package com.mocicarazvan.templatemodule.utils;

import lombok.SneakyThrows;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonoWrapperTest {
    LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(MonoWrapper.class);
        logCaptor.clearLogs();
    }

    @Test
    @SneakyThrows
    void wrapBlockingFunction_success() {
        AtomicBoolean wasRun = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        MonoWrapper.wrapBlockingFunction(
                () -> {
                    wasRun.set(true);
                    latch.countDown();
                }
        );

        latch.await();
        assertTrue(wasRun.get());
    }

    @Test
    @SneakyThrows
    void wrapBlockingFunction_withRetry() {
        Retry retry = Retry.fixedDelay(1, Duration.ofMillis(25));
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean failedOnce = new AtomicBoolean(false);

        MonoWrapper.wrapBlockingFunction(
                () -> {
                    latch.countDown();
                    if (!failedOnce.get()) {
                        failedOnce.set(true);
                        throw new RuntimeException("Test exception");
                    }
                }, retry
        );

        latch.await();
        assertTrue(failedOnce.get());
    }

    @Test
    @SneakyThrows
    void wrapBlockingFunction_error() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean wasRun = new AtomicBoolean(false);

        MonoWrapper.wrapBlockingFunction(
                () -> {
                    wasRun.set(true);
                    latch.countDown();
                    throw new RuntimeException("Test exception");
                }
        );

        latch.await();
        assertTrue(wasRun.get());
        await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertTrue(logCaptor.getErrorLogs().stream().anyMatch(log -> log.contains("Test exception")));
                });
    }

    @Test
    @SneakyThrows
    void wrapBlockingFunction_errorWithRetry() {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger runTimes = new AtomicInteger(0);
        Retry retry = Retry.fixedDelay(1, Duration.ofMillis(25));

        MonoWrapper.wrapBlockingFunction(
                () -> {
                    runTimes.incrementAndGet();
                    latch.countDown();
                    throw new RuntimeException("Test exception");
                }, retry
        );

        latch.await();
        assertEquals(2, runTimes.get());
        await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertTrue(logCaptor.getErrorLogs().stream().anyMatch(log -> log.contains("Test exception")));
                });
    }

}
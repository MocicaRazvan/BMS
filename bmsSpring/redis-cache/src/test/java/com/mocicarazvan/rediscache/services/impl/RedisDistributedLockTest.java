package com.mocicarazvan.rediscache.services.impl;

import com.mocicarazvan.rediscache.config.TestContainersImages;
import com.mocicarazvan.rediscache.services.RedisDistributedLock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@SpringBootTest
@Execution(ExecutionMode.SAME_THREAD)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RedisDistributedLockTest {
    @Container
    @SuppressWarnings("resource")
    public static final GenericContainer<?> redisContainer =
            new GenericContainer<>(TestContainersImages.REDIS_IMAGE)
                    .withExposedPorts(6379).waitingFor(Wait.forListeningPort());
    ;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.custom.cache.redis.host", redisContainer::getHost);
        registry.add("spring.custom.cache.redis.port", redisContainer::getFirstMappedPort);
        registry.add("spring.custom.cache.redis.database", () -> 0);
        registry.add(":spring.custom.executor.redis.async.concurrency.limit", () -> 128);
    }

    @Autowired
    ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    RedisDistributedLock redisDistributedLock;

    private static final String LOCK_KEY = "lock";

    @BeforeEach
    void setUp() {
        redisDistributedLock = new RedisDistributedLockImpl(LOCK_KEY, 20L, reactiveStringRedisTemplate);
    }

    @AfterEach
    void tearDown() {
        reactiveStringRedisTemplate.delete(LOCK_KEY).block();
    }


    @Test
    void tryAcquireLockSequence() {
        StepVerifier.create(redisDistributedLock.tryAcquireLock())
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(redisDistributedLock.tryAcquireLock())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void removeLockSequence() {
        StepVerifier.create(redisDistributedLock.tryAcquireLock())
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(redisDistributedLock.removeLock())
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(redisDistributedLock.removeLock())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void tryAcquireLockSecondInstance() {
        RedisDistributedLock redisDistributedLock2 = new RedisDistributedLockImpl(LOCK_KEY, 20L, reactiveStringRedisTemplate);
        StepVerifier.create(redisDistributedLock.tryAcquireLock())
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(redisDistributedLock2.tryAcquireLock())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void removeLockSecondInstance() {
        RedisDistributedLock redisDistributedLock2 = new RedisDistributedLockImpl(LOCK_KEY, 20L, reactiveStringRedisTemplate);
        StepVerifier.create(redisDistributedLock.tryAcquireLock())
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(redisDistributedLock2.removeLock())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void releaseAcquiredLock() {
        RedisDistributedLock redisDistributedLock2 = new RedisDistributedLockImpl(LOCK_KEY, 20L, reactiveStringRedisTemplate);

        StepVerifier.create(redisDistributedLock.tryAcquireLock())
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(redisDistributedLock.removeLock())
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(redisDistributedLock2.tryAcquireLock())
                .expectNext(true)
                .verifyComplete();
        StepVerifier.create(redisDistributedLock.tryAcquireLock())
                .expectNext(false)
                .verifyComplete();
        StepVerifier.create(redisDistributedLock.removeLock())
                .expectNext(false)
                .verifyComplete();
        StepVerifier.create(redisDistributedLock2.removeLock())
                .expectNext(true)
                .verifyComplete();


    }
}
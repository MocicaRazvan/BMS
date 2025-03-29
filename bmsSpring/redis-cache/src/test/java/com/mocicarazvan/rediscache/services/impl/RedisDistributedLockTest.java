package com.mocicarazvan.rediscache.services.impl;

import com.mocicarazvan.rediscache.containers.AbstractRedisContainer;
import com.mocicarazvan.rediscache.services.RedisDistributedLock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.test.StepVerifier;

@SpringBootTest
class RedisDistributedLockTest extends AbstractRedisContainer {

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
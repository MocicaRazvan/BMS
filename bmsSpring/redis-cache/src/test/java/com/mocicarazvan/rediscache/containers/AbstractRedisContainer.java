package com.mocicarazvan.rediscache.containers;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(parallel = true)
public abstract class AbstractRedisContainer {

    @Container
    @SuppressWarnings("resource")
    public static final GenericContainer<?> redisContainer =
            new GenericContainer<>("eqalpha/keydb:alpine_x86_64_v6.3.4")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.custom.cache.redis.host", redisContainer::getHost);
        registry.add("spring.custom.cache.redis.port", redisContainer::getFirstMappedPort);
        registry.add("spring.custom.cache.redis.database", () -> 0);
        registry.add(":spring.custom.executor.redis.async.concurrency.limit", () -> 128);
    }
}

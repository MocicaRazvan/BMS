package com.mocicarazvan.rediscache.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.config.LocalCacheConfig;
import com.mocicarazvan.rediscache.config.TestContainersImages;
import com.mocicarazvan.rediscache.configTests.TestServiceChildReactive;
import com.mocicarazvan.rediscache.configTests.TestServiceReactive;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.testUtils.AssertionTestUtils;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisChildCacheUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = LocalCacheConfig.class, properties = {
        "spring.custom.cache.redis.expire.minutes=30"
})
@Execution(ExecutionMode.SAME_THREAD)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RedisReactiveCacheChildAspectTest {
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
    TestServiceChildReactive testServiceChildReactive;

    @SpyBean
    ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @SpyBean
    LocalReactiveCache localReactiveCache;

    @SpyBean
    ReverseKeysLocalCache reverseKeysLocalCache;

    @SpyBean
    AspectUtils aspectUtils;

    @SpyBean
    RedisReactiveCacheChildAspect redisReactiveCacheChildAspect;

    @SpyBean
    RedisReactiveChildCacheEvictAspect redisReactiveChildCacheEvictAspect;

    @SpyBean
    RedisChildCacheUtils redisChildCacheUtils;

    ReactiveValueOperations<String, Object> reactiveValueOperations;

    ReactiveSetOperations<String, Object> reactiveSetOperations;


    @SpyBean(name = "objectMapperTest")
    ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        if (reactiveValueOperations != null && reactiveSetOperations != null) {
            reset(
                    localReactiveCache,
                    reactiveValueOperations,
                    reactiveRedisTemplate,
                    reactiveSetOperations,
                    objectMapper,
                    reverseKeysLocalCache,
                    redisReactiveCacheChildAspect
            );
        }
        localReactiveCache.clearAll();
        reverseKeysLocalCache.clearAll();
        reactiveRedisTemplate.execute(connection -> connection.serverCommands().flushAll())
                .blockLast()
        ;


        reactiveValueOperations = spy(reactiveRedisTemplate.opsForValue());
        when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        reactiveSetOperations = spy(reactiveRedisTemplate.opsForSet());
        when(reactiveRedisTemplate.opsForSet()).thenReturn(reactiveSetOperations);
    }

    @Test
    void invalidReturnType_shouldThrow() {
        var ex = assertThrows(RuntimeException.class,
                () -> testServiceChildReactive.invalidReturnType(1L)
        );
        assertEquals(
                "RedisReactiveCacheUpdate: Annotated method has unsupported return type, expected Mono<?> or Flux<?>",
                ex.getMessage()
        );
    }

    @Test
    void addMonoChildToCache_Miss() {
        var res = testServiceChildReactive.getDummies().getFirst();
        StepVerifier.create(testServiceChildReactive.getDummyByIdDefaultMaster(1L))
                .expectNext(res)
                .verifyComplete();
        ArgumentCaptor<String> savingKey = ArgumentCaptor.forClass(String.class);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            verify(redisReactiveCacheChildAspect, times(1)).createBaseMono(savingKey.capture(), any(Method.class));
            verify(redisReactiveCacheChildAspect, times(1)).methodMonoResponseToCache(any(ProceedingJoinPoint.class),
                    eq(TestServiceReactive.CACHE_KEY), eq(savingKey.getValue()), eq(1L), eq(true));
            verify(redisReactiveCacheChildAspect, never()).createBaseFlux(anyString(), any(Method.class));
            verify(redisReactiveCacheChildAspect, never()).methodFluxResponseToCache(any(ProceedingJoinPoint.class),
                    anyString(), anyString(), anyString(), anyBoolean());
        });
    }

    @Test
    void addFluxChildToCache_Miss() {
        var res = testServiceChildReactive.getDummies();
        StepVerifier.create(testServiceChildReactive.getAllDummiesDefaultMaster())
                .expectNextSequence(res)
                .verifyComplete();
        ArgumentCaptor<String> savingKey = ArgumentCaptor.forClass(String.class);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            verify(redisReactiveCacheChildAspect, never()).createBaseMono(anyString(), any(Method.class));
            verify(redisReactiveCacheChildAspect, never()).methodMonoResponseToCache(any(ProceedingJoinPoint.class),
                    anyString(), anyString(), anyLong(), anyBoolean());

            verify(redisReactiveCacheChildAspect, times(1)).createBaseFlux(savingKey.capture(), any(Method.class));
            verify(redisReactiveCacheChildAspect, times(1)).methodFluxResponseToCache(any(ProceedingJoinPoint.class),
                    eq(TestServiceReactive.CACHE_KEY), eq(savingKey.getValue()), eq("id"), eq(true));
        });
    }

    @Test
    void invalidArgsEvict_shouldThrow() {
        StepVerifier.create(testServiceChildReactive.evictDummyByIdInvalidArgs(1L))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException).verify();
    }

    @Test
    void invalidReturnTypeEvict_shouldThrow() {
        var ex = assertThrows(RuntimeException.class, () -> testServiceChildReactive.invalidReturnTypeEvict(1L));
        assertEquals("RedisReactiveCacheChildEvict: Annotated method has invalid return type, expected return type to be Mono<?>", ex.getMessage());
    }

    @Test
    void keysToInvalidateMaster_shouldBeEmpty() {
        StepVerifier.create(redisReactiveChildCacheEvictAspect.keysToInvalidateByMaster("key", null))
                .verifyComplete();
    }

    @Test
    void keysToInvalidateMaster_defaultMaster() {
        var res = testServiceChildReactive.getAllDummiesDefaultMaster().collectList().block();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).until(() -> !Objects.requireNonNull(reactiveRedisTemplate.scan(
                ScanOptions.scanOptions().match("dummies:*").build()
        ).collectList().block()).isEmpty());
        reset(redisChildCacheUtils);
        StepVerifier.create(redisReactiveChildCacheEvictAspect.keysToInvalidateByMaster(TestServiceChildReactive.CACHE_KEY, -1L)
                        .filter(k -> k.contains("dummies:master:-1:child:list"))
                )
                .expectNextCount(1)
                .verifyComplete();
        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(redisChildCacheUtils, times(1)).getMasterKey(anyString(), anyLong());
                });

    }


    private static Stream<Arguments> dummiesIds() {
        return Stream.of(
                Arguments.of(1L),
                Arguments.of(2L),
                Arguments.of(3L)
        );
    }

    @ParameterizedTest
    @MethodSource("dummiesIds")
    void keysToInvalidateMaster_noDefaultMaster(long masterId) {
        var res = testServiceChildReactive.getAllDummiesMaster(String.valueOf(masterId)).collectList().block();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).until(() -> !Objects.requireNonNull(reactiveRedisTemplate.scan(
                ScanOptions.scanOptions().match("dummies:*").build()
        ).collectList().block()).isEmpty());
        reset(redisChildCacheUtils);
        StepVerifier.create(redisReactiveChildCacheEvictAspect.keysToInvalidateByMaster(TestServiceChildReactive.CACHE_KEY, masterId)
                        .filter(k -> k.contains("dummies:master:" + masterId + ":child:list"))
                )
                .expectNextCount(1)
                .verifyComplete();
        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(redisChildCacheUtils, times(2)).getMasterKey(anyString(), anyLong());
                });

    }

    @ParameterizedTest
    @MethodSource("dummiesIds")
    void invalidateForChild_idNotNull(long id) {
        var resF = testServiceChildReactive.getAllDummiesDefaultMaster().collectList().block();
        var resM = testServiceChildReactive.getDummyByIdDefaultMaster(id).block();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).until(() -> Objects.requireNonNull(reactiveRedisTemplate.scan(
                ScanOptions.scanOptions().match("dummies:*").build()
        ).collectList().block()).size() >= 5);
        StepVerifier.create(redisReactiveChildCacheEvictAspect.invalidateForChild(TestServiceChildReactive.CACHE_KEY, id, -1L))
                .expectNext(3L)
                .verifyComplete();
        var reverseKey = redisChildCacheUtils.createReverseIndexKey(TestServiceChildReactive.CACHE_KEY, id);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            verify(reactiveSetOperations, times(1)).members(reverseKey);
            verify(reactiveRedisTemplate, times(1)).delete(reverseKey);
        });

    }

    @ParameterizedTest
    @MethodSource("dummiesIds")
    void invalidateForChild_idNull(long id) {
        var resF = testServiceChildReactive.getAllDummiesDefaultMaster().collectList().block();
        var resM = testServiceChildReactive.getDummyByIdDefaultMaster(id).block();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).until(() -> Objects.requireNonNull(reactiveRedisTemplate.scan(
                ScanOptions.scanOptions().match("dummies:*").build()
        ).collectList().block()).size() >= 5);
        StepVerifier.create(redisReactiveChildCacheEvictAspect.invalidateForChild(TestServiceChildReactive.CACHE_KEY, null, -1L))
                .expectNext(2L)
                .verifyComplete();
        var reverseKey = redisChildCacheUtils.createReverseIndexKey(TestServiceChildReactive.CACHE_KEY, id);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            verify(reactiveSetOperations, never()).members(reverseKey);
            verify(reactiveRedisTemplate, never()).delete(reverseKey);
        });
    }

    @Test
    void invalidateForMaterId_cacheEmpty() {
        var resF = testServiceChildReactive.getAllDummiesMaster("1").collectList().block();
        var resM = testServiceChildReactive.getDummyByIdMaster(1L, "1").block();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).until(() -> Objects.requireNonNull(reactiveRedisTemplate.scan(
                ScanOptions.scanOptions().match("dummies:*").build()
        ).collectList().block()).size() >= 5);

        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            StepVerifier.create(reactiveRedisTemplate.keys("dummies:master:1:*"))
                    .expectNextCount(2)
                    .verifyComplete();
        });
        StepVerifier.create(testServiceChildReactive.evictAllDummiesMasterId("1"))
                .expectNext(true)
                .verifyComplete();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            StepVerifier.create(reactiveRedisTemplate.keys("dummies:master:1:*"))
                    .expectNextCount(0)
                    .verifyComplete();
        });
    }

    @Test
    void invalidateForMaterId_cacheNotTouched() {
        var resF = testServiceChildReactive.getAllDummiesMaster("1").collectList().block();
        var resM = testServiceChildReactive.getDummyByIdMaster(1L, "1").block();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).until(() -> Objects.requireNonNull(reactiveRedisTemplate.scan(
                ScanOptions.scanOptions().match("dummies:*").build()
        ).collectList().block()).size() >= 5);

        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            StepVerifier.create(reactiveRedisTemplate.keys("dummies:master:1:*"))
                    .expectNextCount(2)
                    .verifyComplete();
        });
        StepVerifier.create(testServiceChildReactive.evictAllDummiesMasterId("2"))
                .expectNext(true)
                .verifyComplete();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            StepVerifier.create(reactiveRedisTemplate.keys("dummies:master:1:*"))
                    .expectNextCount(2)
                    .verifyComplete();
        });
    }


}

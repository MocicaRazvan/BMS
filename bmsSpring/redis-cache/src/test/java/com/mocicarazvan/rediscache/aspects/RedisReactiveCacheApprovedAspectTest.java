package com.mocicarazvan.rediscache.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.config.LocalCacheConfig;
import com.mocicarazvan.rediscache.configTests.TestServiceApprovedReactive;
import com.mocicarazvan.rediscache.configTests.TestServiceReactive;
import com.mocicarazvan.rediscache.containers.AbstractRedisContainer;
import com.mocicarazvan.rediscache.enums.BooleanEnum;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import com.mocicarazvan.rediscache.utils.RedisApprovedCacheUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.data.util.Pair;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Set;
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
class RedisReactiveCacheApprovedAspectTest extends AbstractRedisContainer {

    @Autowired
    TestServiceApprovedReactive testServiceApprovedReactive;

    @SpyBean
    ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @SpyBean
    LocalReactiveCache localReactiveCache;

    @SpyBean
    ReverseKeysLocalCache reverseKeysLocalCache;

    @SpyBean
    AspectUtils aspectUtils;

    @SpyBean
    RedisReactiveCacheApprovedAspect redisReactiveCacheApprovedAspect;

    @SpyBean
    RedisReactiveCacheApprovedEvictAspect reactiveCacheApprovedEvictAspect;

    @SpyBean
    RedisApprovedCacheUtils redisApprovedCacheUtils;
    ReactiveValueOperations<String, Object> reactiveValueOperations;

    ReactiveSetOperations<String, Object> reactiveSetOperations;


    @SpyBean(name = "objectMapperTest")
    ObjectMapper objectMapper;
    @Autowired
    private RedisReactiveCacheApprovedEvictAspect redisReactiveCacheApprovedEvictAspect;


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
                    redisReactiveCacheApprovedAspect
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
    public void invalidEvictReturnType_shouldThrow() {
        StepVerifier.create(Mono.defer(() -> testServiceApprovedReactive.invalidateInvalidReturnType(1L)))
                .expectSubscription()
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("RedisReactiveCache: Annotated method has invalid return type, expected return type to be Mono<Pair<?, Boolean>>"))
                .verify();
    }

    @Test
    void addMonoChildToCache_Miss() {
        var res = TestServiceApprovedReactive.dummies.getFirst();
        StepVerifier.create(testServiceApprovedReactive.getDummyById(1L))
                .expectNext(res)
                .verifyComplete();
        ArgumentCaptor<String> savingKey = ArgumentCaptor.forClass(String.class);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            verify(redisReactiveCacheApprovedAspect, times(1)).createBaseMono(savingKey.capture(), any(Method.class));
            verify(redisReactiveCacheApprovedAspect, times(1)).methodMonoResponseToCache(any(ProceedingJoinPoint.class),
                    eq(TestServiceReactive.CACHE_KEY), eq(savingKey.getValue()), eq(1L), eq(true));
            verify(redisReactiveCacheApprovedAspect, never()).createBaseFlux(anyString(), any(Method.class));
            verify(redisReactiveCacheApprovedAspect, never()).methodFluxResponseToCache(any(ProceedingJoinPoint.class),
                    anyString(), anyString(), anyString(), anyBoolean());
        });
    }

    @Test
    void addFluxChildToCache_Miss() {
        var res = TestServiceApprovedReactive.dummies;
        StepVerifier.create(testServiceApprovedReactive.getDummiesDefaults())
                .expectNextSequence(res)
                .verifyComplete();
        ArgumentCaptor<String> savingKey = ArgumentCaptor.forClass(String.class);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            verify(redisReactiveCacheApprovedAspect, never()).createBaseMono(anyString(), any(Method.class));
            verify(redisReactiveCacheApprovedAspect, never()).methodMonoResponseToCache(any(ProceedingJoinPoint.class),
                    anyString(), anyString(), anyLong(), anyBoolean());

            verify(redisReactiveCacheApprovedAspect, times(1)).createBaseFlux(savingKey.capture(), any(Method.class));
            verify(redisReactiveCacheApprovedAspect, times(1)).methodFluxResponseToCache(any(ProceedingJoinPoint.class),
                    eq(TestServiceReactive.CACHE_KEY), eq(savingKey.getValue()), eq("id"), eq(true));
        });
    }

    @Test
    void invalidReturnType_shouldThrow() {
        var ex = assertThrows(RuntimeException.class,
                () -> testServiceApprovedReactive.invalidGetReturnType(1L)
        );
        assertEquals(
                "RedisReactiveCacheApprovedAdd: Annotated method has invalid return type, expected return type to be Mono<?> or Flux<?>",
                ex.getMessage()
        );
    }

    @Test
    void keysToInvalidate_falseApproved_defaultWhom() {
        StepVerifier.create(testServiceApprovedReactive.invalidateCache(1L, false))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
        ArgumentCaptor<Set<String>> keys = ArgumentCaptor.forClass(Set.class);

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(redisApprovedCacheUtils, times(1)).getActualKeys(keys.capture(), any());
                });

        assertEquals(4, keys.getValue().size());
    }

    @Test
    void keysToInvalidate_trueApproved_defaultWhom() {
        StepVerifier.create(testServiceApprovedReactive.invalidateCache(1L, true))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
        ArgumentCaptor<Set<String>> keys = ArgumentCaptor.forClass(Set.class);

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(redisApprovedCacheUtils, times(1)).getActualKeys(keys.capture(), any());
                });

        assertEquals(6, keys.getValue().size());
    }

    @Test
    void keysToInvalidate_falseApproved_trainerWhom() {
        StepVerifier.create(testServiceApprovedReactive.invalidateCache(3L, false))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
        ArgumentCaptor<Set<String>> keys = ArgumentCaptor.forClass(Set.class);

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(redisApprovedCacheUtils, times(1)).getActualKeys(keys.capture(), any());
                });

        assertEquals(4, keys.getValue().size());
    }

    @Test
    void keysToInvalidate_trueApproved_trainerWhom() {
        StepVerifier.create(testServiceApprovedReactive.invalidateCache(3L, true))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
        ArgumentCaptor<Set<String>> keys = ArgumentCaptor.forClass(Set.class);

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(redisApprovedCacheUtils, times(1)).getActualKeys(keys.capture(), any());
                });

        assertEquals(6, keys.getValue().size());
    }

    @Test
    void keysToInvalidate_falseApproved_otherWhom() {
        StepVerifier.create(testServiceApprovedReactive.invalidateCache(2L, false))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
        ArgumentCaptor<Set<String>> keys = ArgumentCaptor.forClass(Set.class);

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(redisApprovedCacheUtils, times(1)).getActualKeys(keys.capture(), any());
                });

        assertEquals(6, keys.getValue().size());
    }

    @Test
    void keysToInvalidate_trueApproved_otherWhom() {
        StepVerifier.create(testServiceApprovedReactive.invalidateCache(2L, true))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
        ArgumentCaptor<Set<String>> keys = ArgumentCaptor.forClass(Set.class);

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(redisApprovedCacheUtils, times(1)).getActualKeys(keys.capture(), any());
                });

        assertEquals(9, keys.getValue().size());
    }

    public static Stream<Arguments> getDummiesAsArgs() {
        return TestServiceApprovedReactive.dummies.stream().map(
                Arguments::of
        );
    }

    @ParameterizedTest
    @MethodSource("getDummiesAsArgs")
    void methodInvalidate_ArgsVerify(TestServiceApprovedReactive.DummyWithApproved dummy) {
        StepVerifier.create(testServiceApprovedReactive.invalidateCache(dummy.id(), dummy.approved()))
                .expectSubscription()
                .expectNextMatches(Pair.of(dummy, dummy.approved())::equals)
                .verifyComplete();

        ArgumentCaptor<TestServiceApprovedReactive.DummyWithApproved> dC =
                ArgumentCaptor.forClass(TestServiceApprovedReactive.DummyWithApproved.class);
        ArgumentCaptor<BooleanEnum> bC = ArgumentCaptor.forClass(BooleanEnum.class);

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(redisReactiveCacheApprovedEvictAspect, times(1))
                            .invalidateForByIdAndOriginalApproved(any(), any(), any(), any(),
                                    dC.capture(), bC.capture()
                            );
                });

        assertEquals(dummy, dC.getValue());
        assertEquals(BooleanEnum.fromBoolean(dummy.approved()), bC.getValue());
    }


}
package com.mocicarazvan.rediscache.aspects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.rediscache.config.LocalCacheConfig;
import com.mocicarazvan.rediscache.config.TestContainersImages;
import com.mocicarazvan.rediscache.configTests.TestServiceReactive;
import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.testUtils.AssertionTestUtils;
import com.mocicarazvan.rediscache.utils.AspectUtils;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
class RedisReactiveCacheAspectTest {
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
    TestServiceReactive testServiceReactive;

    @SpyBean
    ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @SpyBean
    LocalReactiveCache localReactiveCache;

    @SpyBean
    ReverseKeysLocalCache reverseKeysLocalCache;

    @SpyBean
    AspectUtils aspectUtils;

    @SpyBean(name = "redisReactiveCacheAspect")
    RedisReactiveCacheAspect redisReactiveCacheAspect;


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
                    redisReactiveCacheAspect
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
                () -> testServiceReactive.invalidReturnType(1L)
        );
        assertEquals(
                "RedisReactiveCacheUpdate: Annotated method has unsupported return type, expected Mono<?> or Flux<?>",
                ex.getMessage()
        );
    }

    @Test
    @SneakyThrows
    public void addMonoToCacheAllCachesMiss() {
        var res = testServiceReactive.getDummies().getFirst();
        StepVerifier.create(testServiceReactive.getDummyById(1L))
                .expectNext(res)
                .verifyComplete();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(
                        () -> {

                            verify(aspectUtils, atLeastOnce()).extractKeyFromAnnotation(eq(TestServiceReactive.CACHE_KEY), any());
                            verify(aspectUtils, atLeastOnce()).getHashString(any(), eq(TestServiceReactive.CACHE_KEY), eq("getDummyById"));
                            verify(aspectUtils, atLeastOnce()).evaluateSpelExpression(eq("#id"), any());
                            var savingKeyCaptor = ArgumentCaptor.forClass(String.class);
                            verify(redisReactiveCacheAspect, atLeastOnce()).createBaseMono(savingKeyCaptor.capture(), any(Method.class));
                            verify(localReactiveCache, atLeastOnce()).getMonoOrEmpty(savingKeyCaptor.getValue());
                            verify(reactiveValueOperations, atLeastOnce()).get(savingKeyCaptor.getValue());
                            var typeReference = new TypeReference<TestServiceReactive.Dummy>() {
                            };
                            verify(redisReactiveCacheAspect, atLeastOnce()).methodMonoResponseToCache(any(ProceedingJoinPoint.class), eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), anyLong(), eq(true));
                            verify(redisReactiveCacheAspect, atLeastOnce()).saveMonoResultToCache(any(ProceedingJoinPoint.class), eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), anyLong(), eq(res));
                            verify(redisReactiveCacheAspect, atLeastOnce()).saveMonoToCacheNoSubscribe(eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), eq(1L), eq(res));
                            verify(reactiveValueOperations, atLeastOnce()).set(eq(savingKeyCaptor.getValue()), eq(res), eq(Duration.ofMinutes(30)));
                            verify(redisReactiveCacheAspect, atLeastOnce()).addToReverseIndex(TestServiceReactive.CACHE_KEY, 1L, savingKeyCaptor.getValue());

                            var reverseIndexCaptor = ArgumentCaptor.forClass(String.class);
                            verify(reactiveSetOperations, atLeastOnce()).add(reverseIndexCaptor.capture(), eq(savingKeyCaptor.getValue()));
                            await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                                    .untilAsserted(
                                            () -> {
                                                verify(reactiveRedisTemplate, atLeastOnce()).expire(reverseIndexCaptor.getValue(), Duration.ofMinutes(30 + 1));
                                            });
                            verify(reverseKeysLocalCache, atLeastOnce()).add(reverseIndexCaptor.getValue(), savingKeyCaptor.getValue());

                            verify(redisReactiveCacheAspect, never()).createBaseFlux(anyString(), any(Method.class));
                            verify(redisReactiveCacheAspect, never()).methodFluxResponseToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyString(), anyBoolean());
                            verify(objectMapper, never()).convertValue(any(), eq(typeReference));
                            verify(localReactiveCache, never()).put(savingKeyCaptor.capture(), any());

                            StepVerifier.create(reactiveRedisTemplate.opsForValue().get(savingKeyCaptor.getValue())
                                            .map(v -> objectMapper.convertValue(v, typeReference))
                                    )
                                    .expectNext(res)
                                    .verifyComplete();

                            StepVerifier.create(localReactiveCache.getMonoOrEmpty(savingKeyCaptor.getValue()))
                                    .expectNext(res)
                                    .verifyComplete();

                            StepVerifier.create(
                                            reactiveRedisTemplate.opsForSet()
                                                    .members(reverseIndexCaptor.getValue())
                                                    .collectList()
                                    )
                                    .expectNextMatches(redisList -> new HashSet<>(redisList).equals(new HashSet<>(reverseKeysLocalCache.get(reverseIndexCaptor.getValue()))))
                                    .verifyComplete();
                        });

    }

    @Test
    void addMonoLocalCacheHit() {

        var res = testServiceReactive.getDummies().getFirst();

        when(localReactiveCache.getMonoOrEmpty(anyString())).thenReturn(Mono.just(res));


        StepVerifier.create(testServiceReactive.getDummyById(1L))
                .expectNext(res)
                .verifyComplete();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(
                        () -> {
                            verify(aspectUtils, times(1)).extractKeyFromAnnotation(eq(TestServiceReactive.CACHE_KEY), any());
                            verify(aspectUtils, times(1)).getHashString(any(), eq(TestServiceReactive.CACHE_KEY), eq("getDummyById"));
                            verify(aspectUtils, times(1)).evaluateSpelExpression(eq("#id"), any());
                            var savingKeyCaptor = ArgumentCaptor.forClass(String.class);
                            verify(redisReactiveCacheAspect, times(1)).createBaseMono(savingKeyCaptor.capture(), any(Method.class));
                            verify(localReactiveCache, times(1)).getMonoOrEmpty(savingKeyCaptor.getValue());
                            verify(reactiveValueOperations, never()).get(savingKeyCaptor.getValue());
                            var typeReference = new TypeReference<TestServiceReactive.Dummy>() {
                            };


                            verify(redisReactiveCacheAspect, never()).methodMonoResponseToCache(any(ProceedingJoinPoint.class), eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), anyLong(), eq(true));
                            verify(redisReactiveCacheAspect, never()).createBaseFlux(anyString(), any(Method.class));
                            verify(redisReactiveCacheAspect, never()).methodFluxResponseToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyString(), anyBoolean());
                            verify(objectMapper, never()).convertValue(any(), eq(typeReference));
                            verify(localReactiveCache, never()).put(savingKeyCaptor.capture(), any());
                            verify(reactiveValueOperations, never()).get(anyString());
                            verify(localReactiveCache, never()).put(anyString(), any(Object.class));

                            StepVerifier.create(reactiveRedisTemplate.opsForValue().get(savingKeyCaptor.getValue()))
                                    .verifyComplete();

                        });
    }

    @Test
    void addMonoLocalMissRedisHit() {
        var res = testServiceReactive.getDummies().getFirst();

        when(reactiveValueOperations.get(anyString())).thenReturn(Mono.just(res));
        var savingKeyCaptor = ArgumentCaptor.forClass(String.class);
        var typeReference = new TypeReference<TestServiceReactive.Dummy>() {
        };
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(
                        () -> {
                            StepVerifier.create(testServiceReactive.getDummyById(1L))
                                    .expectNext(res)
                                    .verifyComplete();

                            verify(aspectUtils, times(1)).extractKeyFromAnnotation(eq(TestServiceReactive.CACHE_KEY), any());
                            verify(aspectUtils, times(1)).getHashString(any(), eq(TestServiceReactive.CACHE_KEY), eq("getDummyById"));
                            verify(aspectUtils, times(1)).evaluateSpelExpression(eq("#id"), any());
                            verify(redisReactiveCacheAspect, times(1)).createBaseMono(savingKeyCaptor.capture(), any(Method.class));
                            verify(localReactiveCache, times(1)).getMonoOrEmpty(savingKeyCaptor.getValue());


                            verify(redisReactiveCacheAspect, never()).methodMonoResponseToCache(any(ProceedingJoinPoint.class), eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), anyLong(), eq(true));
                            verify(redisReactiveCacheAspect, never()).createBaseFlux(anyString(), any(Method.class));
                            verify(redisReactiveCacheAspect, never()).methodFluxResponseToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyString(), anyBoolean());
                            verify(objectMapper, never()).convertValue(any(), eq(typeReference));
                            verify(localReactiveCache, never()).put(savingKeyCaptor.capture(), any());
                            verify(reactiveValueOperations, times(1)).get(savingKeyCaptor.getValue());
                            verify(localReactiveCache, times(1)).put(savingKeyCaptor.getValue(), res);

                        });
        StepVerifier.create(reactiveRedisTemplate.opsForValue().get(savingKeyCaptor.getValue())
                        .map(v -> objectMapper.convertValue(v, typeReference))
                )
                .expectNext(res)
                .verifyComplete();

        StepVerifier.create(localReactiveCache.getMonoOrEmpty(savingKeyCaptor.getValue()))
                .expectNext(res)
                .verifyComplete();

    }

    @Test
    @SneakyThrows
    public void addMonoToCacheNoSave() {
        var res = testServiceReactive.getDummies().getFirst();
        StepVerifier.create(testServiceReactive.getDummyByIdNoSave(1L))
                .expectNext(res)
                .verifyComplete();
        var savingKeyCaptor = ArgumentCaptor.forClass(String.class);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(
                        () -> {

                            verify(aspectUtils, atLeastOnce()).extractKeyFromAnnotation(eq(TestServiceReactive.CACHE_KEY), any());
                            verify(aspectUtils, atLeastOnce()).getHashString(any(), eq(TestServiceReactive.CACHE_KEY), eq("getDummyByIdNoSave"));
                            verify(aspectUtils, atLeastOnce()).evaluateSpelExpression(eq("#id"), any());
                            verify(redisReactiveCacheAspect, atLeastOnce()).createBaseMono(savingKeyCaptor.capture(), any(Method.class));
                            verify(localReactiveCache, atLeastOnce()).getMonoOrEmpty(savingKeyCaptor.getValue());
                            verify(reactiveValueOperations, atLeastOnce()).get(savingKeyCaptor.getValue());

                            verify(redisReactiveCacheAspect, times(1)).methodMonoResponseToCache(any(ProceedingJoinPoint.class), eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), anyLong(), eq(false));
                            verify(redisReactiveCacheAspect, never()).saveMonoResultToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyLong(), anyBoolean());
                            verify(localReactiveCache, never()).put(savingKeyCaptor.capture(), any());
                            verify(reactiveValueOperations, never()).set(anyString(), any(), any());
                            verify(redisReactiveCacheAspect, never()).addToReverseIndex(anyString(), anyLong(), anyString());
                            verify(reactiveSetOperations, never()).add(anyString(), anyString());
                            verify(reactiveRedisTemplate, never()).expire(anyString(), any());
                            verify(reverseKeysLocalCache, never()).add(anyString(), anyString());


                        });
        StepVerifier.create(reactiveRedisTemplate.opsForValue().get(savingKeyCaptor.getValue())
                )
                .verifyComplete();

        StepVerifier.create(localReactiveCache.getMonoOrEmpty(savingKeyCaptor.getValue()))
                .verifyComplete();

    }

    @Test
    public void addMonoToCacheNoIdAnn() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            testServiceReactive.getDummyByIdNoIdAnn(1L).block();
        });

        assertEquals(
                "RedisReactiveCache: Annotated method has invalid idSpel, expected idSpel not null or empty",
                ex.getMessage()
        );
    }


    @Test
    @SneakyThrows
    public void addFluxToCacheAllCachesMiss() {
        var res = testServiceReactive.getDummies();
        StepVerifier.create(testServiceReactive.getAllDummies().collectList())
                .expectNextMatches(
                        r -> new HashSet<>(r).equals(new HashSet<>(res))
                )
                .verifyComplete();
        var savingKeyCaptor = ArgumentCaptor.forClass(String.class);
        var reverseIndexCaptor = ArgumentCaptor.forClass(String.class);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(
                        () -> {

                            verify(aspectUtils, atLeastOnce()).extractKeyFromAnnotation(eq(TestServiceReactive.CACHE_KEY), any());
                            verify(aspectUtils, atLeastOnce()).getHashString(any(), eq(TestServiceReactive.CACHE_KEY), eq("getAllDummies"));
                            verify(redisReactiveCacheAspect, atLeastOnce()).createBaseFlux(savingKeyCaptor.capture(), any(Method.class));
                            verify(localReactiveCache, atLeastOnce()).getFluxOrEmpty(savingKeyCaptor.getValue());
                            verify(reactiveValueOperations, atLeastOnce()).get(savingKeyCaptor.getValue());
                            var typeReference = new TypeReference<TestServiceReactive.Dummy>() {
                            };


                            verify(reactiveValueOperations, atLeastOnce()).set(eq(savingKeyCaptor.getValue()), eq(res), eq(Duration.ofMinutes(30)));
                            verify(redisReactiveCacheAspect, atLeastOnce()).addToReverseIndex(TestServiceReactive.CACHE_KEY, 1L, savingKeyCaptor.getValue());
                            verify(redisReactiveCacheAspect, atLeastOnce()).addToReverseIndex(TestServiceReactive.CACHE_KEY, 2L, savingKeyCaptor.getValue());
                            verify(redisReactiveCacheAspect, atLeastOnce()).addToReverseIndex(TestServiceReactive.CACHE_KEY, 3L, savingKeyCaptor.getValue());
                            verify(redisReactiveCacheAspect, atLeastOnce()).methodFluxResponseToCache(any(ProceedingJoinPoint.class), eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), anyString(), eq(true));

                            verify(reactiveSetOperations, atLeast(res.size())).add(reverseIndexCaptor.capture(), eq(savingKeyCaptor.getValue()));
                            await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                                    .untilAsserted(
                                            () -> {
                                                verify(reactiveRedisTemplate, atLeastOnce()).expire(reverseIndexCaptor.getValue(), Duration.ofMinutes(30 + 1));
                                            });
                            verify(reverseKeysLocalCache, atLeastOnce()).add(reverseIndexCaptor.getValue(), savingKeyCaptor.getValue());
                            verify(reactiveValueOperations, atLeastOnce())
                                    .set(eq(savingKeyCaptor.getValue()), eq(res), eq(Duration.ofMinutes(30)));

                            verify(reactiveRedisTemplate, atLeastOnce())
                                    .expire(reverseIndexCaptor.getValue(), Duration.ofMinutes(30 + 1));


                            verify(aspectUtils, never()).evaluateSpelExpression(anyString(), any());
                            verify(redisReactiveCacheAspect, never()).methodMonoResponseToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyLong(), anyBoolean());
                            verify(redisReactiveCacheAspect, never()).saveMonoResultToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyLong(), anyBoolean());
                            verify(redisReactiveCacheAspect, never()).saveMonoToCacheNoSubscribe(eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), eq(1L), eq(res));

                            verify(objectMapper, never()).convertValue(any(), eq(typeReference));
                            verify(localReactiveCache, atLeastOnce()).put(eq(savingKeyCaptor.getValue()), anyList());


                        });
        StepVerifier.create(localReactiveCache.getFluxOrEmpty(savingKeyCaptor.getValue()).collectList())
                .expectNextMatches(res::equals)
                .verifyComplete();

        StepVerifier.create(
                        reactiveRedisTemplate.opsForSet()
                                .members(reverseIndexCaptor.getValue())
                                .collectList()
                )
                .expectNextMatches(redisList -> new HashSet<>(redisList).equals(new HashSet<>(reverseKeysLocalCache.get(reverseIndexCaptor.getValue()))))
                .verifyComplete();
        StepVerifier.create(reactiveRedisTemplate.opsForValue().get(savingKeyCaptor.getValue())
                        .map(v -> objectMapper.convertValue(v, objectMapper.getTypeFactory()
                                .constructCollectionType(List.class,
                                        objectMapper.getTypeFactory().constructType(TestServiceReactive.Dummy.class)
                                )))
                )
                .expectNext(res)
                .verifyComplete();

    }

    @Test
    void addFluxLocalCacheHit() {
        var res = testServiceReactive.getDummies();

        when(localReactiveCache.getFluxOrEmpty(anyString())).thenReturn(Flux.fromIterable(res));

        StepVerifier.create(testServiceReactive.getAllDummies().collectList())
                .expectNextMatches(
                        r -> new HashSet<>(r).equals(new HashSet<>(res))
                )
                .verifyComplete();
        var savingKeyCaptor = ArgumentCaptor.forClass(String.class);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(
                        () -> {
                            verify(aspectUtils, times(1)).extractKeyFromAnnotation(eq(TestServiceReactive.CACHE_KEY), any());
                            verify(aspectUtils, times(1)).getHashString(any(), eq(TestServiceReactive.CACHE_KEY), eq("getAllDummies"));
                            verify(redisReactiveCacheAspect, times(1)).createBaseFlux(savingKeyCaptor.capture(), any(Method.class));
                            verify(localReactiveCache, times(1)).getFluxOrEmpty(savingKeyCaptor.getValue());
                            verify(reactiveValueOperations, never()).get(savingKeyCaptor.getValue());
                            var typeReference = new TypeReference<TestServiceReactive.Dummy>() {
                            };

                            verify(reactiveValueOperations, never()).set(eq(savingKeyCaptor.getValue()), eq(res), eq(Duration.ofMinutes(30)));
                            verify(redisReactiveCacheAspect, never()).addToReverseIndex(TestServiceReactive.CACHE_KEY, 1L, savingKeyCaptor.getValue());
                            verify(redisReactiveCacheAspect, never()).addToReverseIndex(TestServiceReactive.CACHE_KEY, 2L, savingKeyCaptor.getValue());
                            verify(redisReactiveCacheAspect, never()).addToReverseIndex(TestServiceReactive.CACHE_KEY, 3L, savingKeyCaptor.getValue());

                            verify(redisReactiveCacheAspect, never()).methodFluxResponseToCache(any(ProceedingJoinPoint.class), eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), anyString(), eq(true));
                            verify(reactiveSetOperations, never()).add(anyString(), anyString());
                            verify(reactiveRedisTemplate, never()).expire(anyString(), any());
                            verify(reverseKeysLocalCache, never()).add(anyString(), anyString());
                            verify(reactiveValueOperations, never()).set(anyString(), any(), any());
                            verify(reactiveRedisTemplate, never()).expire(anyString(), any());
                            verify(aspectUtils, never()).evaluateSpelExpression(anyString(), any());
                            verify(redisReactiveCacheAspect, never()).methodMonoResponseToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyLong(), anyBoolean());
                            verify(redisReactiveCacheAspect, never()).saveMonoResultToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyLong(), anyBoolean());
                            verify(redisReactiveCacheAspect, never()).saveMonoToCacheNoSubscribe(eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), eq(1L), eq(res));
                            verify(objectMapper, never()).convertValue(any(), eq(typeReference));
                            verify(localReactiveCache, never()).put(eq(savingKeyCaptor.getValue()), anyList());
                        });

        StepVerifier.create(reactiveRedisTemplate.opsForValue().get(savingKeyCaptor.getValue())
                )
                .verifyComplete();


        StepVerifier.create(localReactiveCache.getFluxOrEmpty(savingKeyCaptor.getValue()))
                .expectNextSequence(res)
                .verifyComplete();

        StepVerifier.create(reactiveRedisTemplate.opsForSet().members(savingKeyCaptor.getValue()))
                .verifyComplete();
    }

    @Test
    void addFluxLocalMissRedisHit() {
        var res = testServiceReactive.getDummies();
        var serialized = objectMapper.convertValue(res, Object.class);

        when(reactiveValueOperations.get(anyString())).thenReturn(Mono.just(serialized));

        StepVerifier.create(testServiceReactive.getAllDummies().collectList())
                .expectNextMatches(
                        r -> new HashSet<>(r).equals(new HashSet<>(res))
                )
                .verifyComplete();
        var savingKeyCaptor = ArgumentCaptor.forClass(String.class);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(
                        () -> {
                            verify(aspectUtils, times(1)).extractKeyFromAnnotation(eq(TestServiceReactive.CACHE_KEY), any());
                            verify(aspectUtils, times(1)).getHashString(any(), eq(TestServiceReactive.CACHE_KEY), eq("getAllDummies"));
                            verify(redisReactiveCacheAspect, times(1)).createBaseFlux(savingKeyCaptor.capture(), any(Method.class));
                            verify(localReactiveCache, times(1)).getFluxOrEmpty(savingKeyCaptor.getValue());
                            verify(reactiveValueOperations, times(1)).get(savingKeyCaptor.getValue());
                            var typeReference = new TypeReference<TestServiceReactive.Dummy>() {
                            };

                            verify(redisReactiveCacheAspect, never()).methodFluxResponseToCache(any(ProceedingJoinPoint.class), eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), anyString(), eq(true));
                            verify(reactiveSetOperations, never()).add(anyString(), anyString());
                            verify(reactiveRedisTemplate, never()).expire(anyString(), any());
                            verify(reactiveValueOperations, never()).set(anyString(), any(), any());
                            verify(reactiveRedisTemplate, never()).expire(anyString(), any());
                            verify(aspectUtils, never()).evaluateSpelExpression(anyString(), any());
                            verify(redisReactiveCacheAspect, never()).methodMonoResponseToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyLong(), anyBoolean());
                            verify(redisReactiveCacheAspect, never()).saveMonoResultToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyLong(), anyBoolean());
                            verify(redisReactiveCacheAspect, never()).saveMonoToCacheNoSubscribe(eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), eq(1L), eq(res));
                            verify(objectMapper, never()).convertValue(any(), eq(typeReference));
                            verify(localReactiveCache, times(1)).put(eq(savingKeyCaptor.getValue()), anyList());
                        });

        StepVerifier.create(reactiveRedisTemplate.opsForValue().get(savingKeyCaptor.getValue())
                        .map(v -> objectMapper.convertValue(v, objectMapper.getTypeFactory()
                                .constructCollectionType(List.class,
                                        objectMapper.getTypeFactory().constructType(TestServiceReactive.Dummy.class)
                                )))
                )
                .expectNextMatches(res::equals)
                .verifyComplete();

        StepVerifier.create(localReactiveCache.getFluxOrEmpty(savingKeyCaptor.getValue()))
                .expectNextSequence(res)
                .verifyComplete();

    }

    @Test
    void addFluxToCacheNoSave() {
        var res = testServiceReactive.getDummies();
        StepVerifier.create(testServiceReactive.getAllDummiesNoSave().collectList())
                .expectNextMatches(
                        r -> new HashSet<>(r).equals(new HashSet<>(res))
                )
                .verifyComplete();
        var savingKeyCaptor = ArgumentCaptor.forClass(String.class);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(
                        () -> {

                            verify(aspectUtils, atLeastOnce()).extractKeyFromAnnotation(eq(TestServiceReactive.CACHE_KEY), any());
                            verify(aspectUtils, atLeastOnce()).getHashString(any(), eq(TestServiceReactive.CACHE_KEY), eq("getAllDummiesNoSave"));
                            verify(redisReactiveCacheAspect, atLeastOnce()).createBaseFlux(savingKeyCaptor.capture(), any(Method.class));
                            verify(localReactiveCache, atLeastOnce()).getFluxOrEmpty(savingKeyCaptor.getValue());
                            verify(reactiveValueOperations, atLeastOnce()).get(savingKeyCaptor.getValue());
                            var typeReference = new TypeReference<TestServiceReactive.Dummy>() {
                            };

                            verify(redisReactiveCacheAspect, atLeastOnce()).methodFluxResponseToCache(any(ProceedingJoinPoint.class), eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), anyString(), eq(false));
                            verify(reactiveValueOperations, never()).set(eq(savingKeyCaptor.getValue()), eq(res), eq(Duration.ofMinutes(30)));
                            verify(redisReactiveCacheAspect, never()).addToReverseIndex(TestServiceReactive.CACHE_KEY, 1L, savingKeyCaptor.getValue());
                            verify(redisReactiveCacheAspect, never()).addToReverseIndex(TestServiceReactive.CACHE_KEY, 2L, savingKeyCaptor.getValue());
                            verify(redisReactiveCacheAspect, never()).addToReverseIndex(TestServiceReactive.CACHE_KEY, 3L, savingKeyCaptor.getValue());
                            verify(reactiveSetOperations, never()).add(anyString(), anyString());
                            verify(reactiveRedisTemplate, never()).expire(anyString(), any());
                            verify(reverseKeysLocalCache, never()).add(anyString(), anyString());

                            verify(reactiveValueOperations, never()).set(anyString(), any(), any());
                            verify(reactiveRedisTemplate, never()).expire(anyString(), any());
                            verify(aspectUtils, never()).evaluateSpelExpression(anyString(), any());
                            verify(redisReactiveCacheAspect, never()).methodMonoResponseToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyLong(), anyBoolean());
                            verify(redisReactiveCacheAspect, never()).saveMonoResultToCache(any(ProceedingJoinPoint.class), anyString(), anyString(), anyLong(), anyBoolean());
                            verify(redisReactiveCacheAspect, never()).saveMonoToCacheNoSubscribe(eq(TestServiceReactive.CACHE_KEY), eq(savingKeyCaptor.getValue()), eq(1L), eq(res));
                            verify(objectMapper, never()).convertValue(any(), eq(typeReference));
                            verify(localReactiveCache, never()).put(eq(savingKeyCaptor.getValue()), anyList());
                        });

        StepVerifier.create(reactiveRedisTemplate.opsForValue().get(savingKeyCaptor.getValue())
                )
                .verifyComplete();

        StepVerifier.create(localReactiveCache.getFluxOrEmpty(savingKeyCaptor.getValue()))
                .verifyComplete();
    }

    @Test
    void addFluxToCacheNoIdAnn() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            testServiceReactive.getAllDummiesNoIdAnn().blockLast();
        });

        assertEquals(
                "RedisReactiveCache: Annotated method has invalid idSpel, expected idSpel not null or empty",
                ex.getMessage()
        );
    }


    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L})
    void cacheInvalidateMono_cacheShouldBeEmpty(long id) {

        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            testServiceReactive.getDummyById(id).block();
            var members = reactiveRedisTemplate.opsForSet()
                    .members("dummies:" + id)
                    .collectList()
                    .block();
            var reverseLocal = reverseKeysLocalCache.getMap().containsKey("dummies:" + id);
            assert members != null && !members.isEmpty() && reverseLocal;

        });
        StepVerifier.create(testServiceReactive.evictDummyById(id))
                .expectNext(testServiceReactive.getDummies().stream().filter(
                                d -> d.id().equals(id)
                        ).findFirst().orElseThrow()
                )
                .verifyComplete();
        StepVerifier.create(reactiveRedisTemplate.scan(ScanOptions.scanOptions()
                        .match("dummies:*")
                        .build()))
                .verifyComplete();
        var local = localReactiveCache.getAll();
        assertEquals(0, local.size());
        var reverse = reverseKeysLocalCache.getAll();
        assertEquals(0, reverse.size());

    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L})
    void cacheInvalidateMono_cacheShouldNotBeAffected(long id) {
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            var res = testServiceReactive.getDummyById(id).block();
            var lastTwoDummies = testServiceReactive.getDummies().subList(1, 3);
            assert res != null;
            StepVerifier.create(testServiceReactive.evictDummyById(id + 1))
                    .expectNext(
                            lastTwoDummies.stream().filter(d -> d.id().equals(id + 1)).findFirst().orElseThrow()
                    )
                    .verifyComplete();
            StepVerifier.create(reactiveRedisTemplate.scan(ScanOptions.scanOptions()
                            .match("dummies:*")
                            .build()))
                    .expectNextCount(2)
                    .verifyComplete();
            var local = localReactiveCache.getAll();
            assertEquals(1, local.size());
            var reverse = reverseKeysLocalCache.getAll();
            assertEquals(1, reverse.size());
        });

    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L})
    void cacheInvalidateFlux_cacheEmpty(long id) {
        var res = testServiceReactive.getAllDummies().collectList().block();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).until(() -> {
            var members = reactiveRedisTemplate.opsForSet()
                    .members("dummies:" + id)
                    .collectList()
                    .block();
            var reverseLocal = reverseKeysLocalCache.getMap().containsKey("dummies:" + id);
            return members != null && !members.isEmpty() && reverseLocal;
        });
        StepVerifier.create(testServiceReactive.evictDummyById(id))
                .expectNextCount(1)
                .verifyComplete();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            StepVerifier.create(reactiveRedisTemplate.scan(ScanOptions.scanOptions()
                                    .match("*")
                                    .build())
                            .filter(k -> k.contains("dummies:" + id) && k.contains("list"))
                    )
                    .verifyComplete();
        });
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            var local = localReactiveCache.getAll();
            assertEquals(0, local.size());
            var reverse = reverseKeysLocalCache.getAll();
            assertEquals(2, reverse.size());
        });


    }

    @Test
    void cacheInvalidateFlux_cacheNotTouched() {
        long id = 4L;
        var res = testServiceReactive.getAllDummies().collectList().block();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).until(() -> {
            assert res != null;
            var members = res.stream().flatMap(k ->
                    Objects.requireNonNull(reactiveRedisTemplate.opsForSet()
                            .members("dummies:" + k.id())
                            .collectList()
                            .block()).stream()
            ).filter(Objects::nonNull).collect(Collectors.toSet());
            var reverseLocal = reverseKeysLocalCache.getMap().size() == 3;
            System.out.println(members);
            return members.size() == 1 && reverseLocal;
        });
        StepVerifier.create(testServiceReactive.evictDummyById(id))
                .expectNextCount(0)
                .verifyComplete();
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            StepVerifier.create(reactiveRedisTemplate.scan(ScanOptions.scanOptions()
                            .match("*")
                            .build())
                    )
                    .expectNextCount(4)
                    .verifyComplete();
        });
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS).untilAsserted(() -> {
            var local = localReactiveCache.getAll();
            assertEquals(1, local.size());
            var reverse = reverseKeysLocalCache.getAll();
            assertEquals(3, reverse.size());
        });


    }

    @Test
    void cacheEvictInvalidId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            testServiceReactive.evictDummyByIdInvalidId(1L).block();
        });

        assertEquals(
                "RedisReactiveCache: Annotated method has invalid idSpel, expected idSpel not null or empty",
                ex.getMessage()
        );
    }

    @Test
    void cacheEvictInvalidReturnType() {
        var ex = assertThrows(RuntimeException.class,
                () -> testServiceReactive.invalidReturnTypeEvict(1L)
        );
        assertEquals(
                "RedisReactiveCacheEvict: Annotated method has unsupported return type, expected Mono<?> or Flux<?>",
                ex.getMessage()
        );
    }
}
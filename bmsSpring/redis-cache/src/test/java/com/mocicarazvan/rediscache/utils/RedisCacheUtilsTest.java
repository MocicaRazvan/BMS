package com.mocicarazvan.rediscache.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ScanOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheUtilsTest {

    @Mock
    private AspectUtils aspectUtils;
    @Mock
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @InjectMocks
    private RedisCacheUtils redisCacheUtils;

    @Test
    void checkValidId_valid() {
        assertDoesNotThrow(() -> redisCacheUtils.checkValidId("abc"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void checkValidId_invalid(String id) {
        assertThrows(IllegalArgumentException.class, () -> redisCacheUtils.checkValidId(id));
    }

    @Test
    void createReverseIndexKey_concatenatesKeyAndId() {
        String result = redisCacheUtils.createReverseIndexKey("testKey", 123L);
        assertEquals("testKey:123", result);
    }

    @Test
    void getSingleKey_returnsSingleKeyFormat() {
        String result = redisCacheUtils.getSingleKey("key", 456L);
        assertEquals("key:single:456", result);
    }

    @Test
    void getListKey_returnsListKeyFormat() {
        String result = redisCacheUtils.getListKey("key");
        assertEquals("key:list", result);
    }

    @Test
    void getHashKey_returnsHashKeyFormat() {
        String result = redisCacheUtils.getHashKey("argsHash");
        assertEquals(":hash:argsHash", result);
    }

    @Test
    void getActualKeys_returnsMatchingKeys() {
        List<String> patterns = List.of("pattern*");
        ScanOptions options = ScanOptions.scanOptions()
                .type(DataType.STRING)
                .count(50)
                .match("pattern*").build();
        when(reactiveRedisTemplate.scan(any(ScanOptions.class))).thenReturn(Flux.just("pattern1", "pattern2"));

        StepVerifier.create(redisCacheUtils.getActualKeys(patterns, reactiveRedisTemplate))
                .expectNext("pattern1", "pattern2")
                .verifyComplete();
    }

    @Test
    void getOptionalIdDelete_withBlankIdSpel_returnsDefaultValues() {
        Tuple3<Flux<String>, Mono<Long>, Long> result = redisCacheUtils.getOptionalIdDelete(joinPoint, "key", "");
        StepVerifier.create(result.getT1()).expectComplete().verify();
        StepVerifier.create(result.getT2()).expectComplete().verify();
        assertEquals(-100L, result.getT3());
    }

    @Test
    void getOptionalIdDelete_withValidIdSpel_returnsMembersAndDeleteMono() {
        when(aspectUtils.evaluateSpelExpression("spel", joinPoint)).thenReturn("123");
        when(aspectUtils.assertLong("123")).thenReturn(123L);

        ReactiveSetOperations<String, Object> setOps = mock(ReactiveSetOperations.class);
        when(reactiveRedisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.members("key:123")).thenReturn(Flux.just("member1", "member2"));
        when(reactiveRedisTemplate.delete("key:123")).thenReturn(Mono.just(1L));

        Tuple3<Flux<String>, Mono<Long>, Long> result = redisCacheUtils.getOptionalIdDelete(joinPoint, "key", "spel");
        StepVerifier.create(result.getT1())
                .expectNext("member1", "member2")
                .verifyComplete();
        StepVerifier.create(result.getT2())
                .expectNext(1L)
                .verifyComplete();
        assertEquals(123L, result.getT3());
    }

    @Test
    void deleteListFromRedis_withEmptyList_returnsZero() {
        StepVerifier.create(redisCacheUtils.deleteListFromRedis(Collections.emptyList()))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void deleteListFromRedis_withKeys_returnsDeletedCount() {
        List<String> keys = List.of("key1", "key2");
        when(reactiveRedisTemplate.delete(any(String[].class))).thenReturn(Mono.just(2L));
        StepVerifier.create(redisCacheUtils.deleteListFromRedis(keys))
                .expectNext(2L)
                .verifyComplete();
    }
}
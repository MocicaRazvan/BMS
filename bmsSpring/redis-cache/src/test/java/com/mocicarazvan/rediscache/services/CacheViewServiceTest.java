package com.mocicarazvan.rediscache.services;

import com.mocicarazvan.rediscache.config.FlushProperties;
import com.mocicarazvan.rediscache.config.LocalCacheConfig;
import com.mocicarazvan.rediscache.containers.AbstractRedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Import(LocalCacheConfig.class)
class CacheViewServiceTest extends AbstractRedisContainer {

    @SpyBean
    CacheViewService cacheViewService;
    @SpyBean
    FlushProperties flushProperties;
    @SpyBean
    ReactiveStringRedisTemplate reactiveRedisTemplate;

    RedisDistributedLock redisDistributedLock;

    private String viewKeyPrefix;
    private String getKeyPrefix;


    @BeforeEach
    void setUp() {
        var realLock = ReflectionTestUtils.getField(cacheViewService, "redisDistributedLock");
        if (realLock == null) {
            throw new RuntimeException("redisDistributedLock is null");
        }
//        redisDistributedLock = spy((RedisDistributedLock) realLock);
//
//        ReflectionTestUtils.setField(cacheViewService, "redisDistributedLock", redisDistributedLock);
        redisDistributedLock = mock(RedisDistributedLock.class);
        ReflectionTestUtils.setField(cacheViewService, "redisDistributedLock", redisDistributedLock);
        viewKeyPrefix = Objects.requireNonNull(ReflectionTestUtils.getField(cacheViewService, "viewKeyPrefix")).toString();
        getKeyPrefix = Objects.requireNonNull(ReflectionTestUtils.getField(cacheViewService, "getKeyPrefix")).toString();
    }

    @Test
    void shouldNotScheduleFlush() {
        when(
                flushProperties.isEnabled()
        ).thenReturn(false);
        cacheViewService.scheduleFlush();

        verify(redisDistributedLock, never()).tryAcquireLock();
        verify(cacheViewService, never()).flushViewCountForKey(anyString());
        verify(redisDistributedLock, never()).removeLock();
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4, 5})
    void shouldScheduleFlush(Long multiple) {
        var scheduler = VirtualTimeScheduler.getOrSet();
        var keys = List.of("key1", "key2");

        var timeout = 10L;
        when(flushProperties.getTimeout()).thenReturn(timeout);
        when(flushProperties.getParallelism()).thenReturn(1);
        when(redisDistributedLock.tryAcquireLock()).thenReturn(Mono.just(true));
        when(cacheViewService.scanKeys(anyString())).thenReturn(Flux.fromIterable(keys));
        when(cacheViewService.flushViewCountForKey(anyString())).thenReturn(Mono.empty());
        when(redisDistributedLock.removeLock()).thenReturn(Mono.just(true));

        cacheViewService.scheduleFlush();

        scheduler.advanceTimeBy(Duration.ofSeconds(multiple * timeout + 1));

        var keysCaptor = ArgumentCaptor.forClass(String.class);


        verify(redisDistributedLock, atLeastOnce()).tryAcquireLock();
        verify(cacheViewService, atLeastOnce()).flushViewCountForKey(keysCaptor.capture());
        verify(redisDistributedLock, atLeastOnce()).removeLock();

        assertLinesMatch(new HashSet<>(keys).stream().toList(), new HashSet<>(keysCaptor.getAllValues()).stream().toList());
        assertTrue(multiple * keys.size() <= keysCaptor.getAllValues().size());
    }

    @Test
    void shouldNotScheduleFlushWhenLockNotAcquired() {
        var scheduler = VirtualTimeScheduler.getOrSet();

        when(flushProperties.getTimeout()).thenReturn(1L);
        when(redisDistributedLock.tryAcquireLock()).thenReturn(Mono.just(false));
        cacheViewService.scheduleFlush();

        scheduler.advanceTimeBy(Duration.ofSeconds(2));

        verify(redisDistributedLock, atLeastOnce()).tryAcquireLock();
        verify(cacheViewService, never()).flushViewCountForKey(anyString());
        verify(redisDistributedLock, never()).removeLock();
    }

    @Test
    void incrementView() {
        var id = 1L;

        when(flushProperties.isEnabled()).thenReturn(false);
        StepVerifier.create(cacheViewService.incrementView(id))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(reactiveRedisTemplate.opsForValue().get(viewKeyPrefix + id))
                .expectNext("1")
                .verifyComplete();


        StepVerifier.create(cacheViewService.incrementView(id))
                .expectNext(2L)
                .verifyComplete();

        StepVerifier.create(reactiveRedisTemplate.opsForValue().get(viewKeyPrefix + id))
                .expectNext("2")
                .verifyComplete();

        verify(reactiveRedisTemplate, times(4)).opsForValue();
    }


    @Test
    void testGetCacheWhenValueExists() {
        Long itemId = 1L;
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now();
        String key = getKeyPrefix + itemId + ":" + start + ":" + end;
        String cachedValue = "42";

        ReactiveValueOperations<String, String> opsForValue = mock(ReactiveValueOperations.class);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(opsForValue);
        when(opsForValue.get(key)).thenReturn(Mono.just(cachedValue));

        Mono<Long> result = cacheViewService.getCache(itemId, start, end);

        StepVerifier.create(result)
                .expectNext(42L)
                .verifyComplete();

        verify(opsForValue, times(1)).get(key);
        verify(opsForValue, never()).set(any(), any(), any());
        verify(cacheViewService, never()).getCacheBase(anyLong(), any(), any());
    }

    @Test
    void testGetCacheWhenValueAbsent() {
        Long itemId = 1L;
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now();
        String key = getKeyPrefix + itemId + ":" + start + ":" + end;
        Long baseCount = 55L;
        long flushTimeout = 10L;

        when(flushProperties.getTimeout()).thenReturn(flushTimeout);

        ReactiveValueOperations<String, String> opsForValue = mock(ReactiveValueOperations.class);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(opsForValue);
        when(opsForValue.get(key)).thenReturn(Mono.empty());

        doReturn(Mono.just(baseCount)).when(cacheViewService).getCacheBase(itemId, start, end);

        when(opsForValue.set(eq(key), eq(baseCount.toString()), eq(Duration.ofSeconds(flushTimeout / 2))))
                .thenReturn(Mono.just(true));

        Mono<Long> result = cacheViewService.getCache(itemId, start, end);

        StepVerifier.create(result)
                .expectNext(baseCount)
                .verifyComplete();

        verify(opsForValue, times(1)).get(key);
        verify(cacheViewService, times(1)).getCacheBase(itemId, start, end);
        verify(opsForValue, times(1)).set(eq(key), eq(baseCount.toString()), any());
    }


    @Test
    void flushViewCacheValueNotPresent() {
        String key = viewKeyPrefix + 1L;
        ReactiveValueOperations<String, String> opsForValue = mock(ReactiveValueOperations.class);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(opsForValue);
        when(opsForValue.get(key)).thenReturn(Mono.empty());

        StepVerifier.create(cacheViewService.flushViewCountForKey(key))
                .verifyComplete();

        verify(cacheViewService, never()).flushCache(anyLong(), anyLong());
        verify(reactiveRedisTemplate, never()).delete(anyString());
    }

    @Test
    void flushViewCacheValuePresent() {
        String key = viewKeyPrefix + 1L;

        ReactiveValueOperations<String, String> opsForValue = mock(ReactiveValueOperations.class);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(opsForValue);
        when(opsForValue.get(key)).thenReturn(Mono.just("42"));

        StepVerifier.create(cacheViewService.flushViewCountForKey(key))
                .expectNext(0L)
                .verifyComplete();

        verify(cacheViewService, times(1)).flushCache(1L, 42L);
        verify(reactiveRedisTemplate, times(1)).delete(key);
    }
}
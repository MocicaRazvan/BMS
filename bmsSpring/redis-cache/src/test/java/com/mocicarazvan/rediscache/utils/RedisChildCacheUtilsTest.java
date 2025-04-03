package com.mocicarazvan.rediscache.utils;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RedisChildCacheUtilsTest {


    @Mock
    private AspectUtils aspectUtils;
    @Mock
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    @InjectMocks
    private RedisChildCacheUtils redisChildCacheUtils;


    public static Stream<Arguments> keyArgs() {
        return Stream.of(
                Arguments.of("cacheKey", 123L, "cacheKey:master:123:"),
                Arguments.of("", 456L, ":master:456:"),
                Arguments.of(null, 789L, "null:master:789:"),
                Arguments.of("cacheKey", null, "cacheKey:master:null:"),
                Arguments.of(null, null, "null:master:null:")
        );
    }

    @ParameterizedTest
    @MethodSource("keyArgs")
    void returnsConcatenatedKey(
            String key,
            Long mId,
            String expected
    ) {
        String actual = redisChildCacheUtils.getMasterKey(key, mId);
        assertEquals(expected, actual);
    }


}
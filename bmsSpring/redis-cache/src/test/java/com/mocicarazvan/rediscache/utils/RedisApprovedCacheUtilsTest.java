package com.mocicarazvan.rediscache.utils;

import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCache;
import com.mocicarazvan.rediscache.enums.BooleanEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RedisApprovedCacheUtilsTest {

    @Mock
    private AspectUtils aspectUtils;

    @Mock
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @InjectMocks
    private RedisApprovedCacheUtils redisApprovedCacheUtils;
    @Mock
    RedisReactiveApprovedCache annotation;

    @Mock
    ProceedingJoinPoint joinPoint;

    @Test
    void returnsAnnotationApprovedWhenSpelExpressionPathIsNull() {
        Mockito.when(annotation.approvedArgumentPath()).thenReturn(null);
        Mockito.when(annotation.approved()).thenReturn(BooleanEnum.TRUE);

        BooleanEnum result = redisApprovedCacheUtils.getApprovedArg(joinPoint, annotation);
        assertEquals(BooleanEnum.TRUE, result);
    }

    @Test
    void returnsAnnotationApprovedWhenSpelExpressionPathIsBlank() {
        Mockito.when(annotation.approvedArgumentPath()).thenReturn("  ");
        Mockito.when(annotation.approved()).thenReturn(BooleanEnum.FALSE);

        BooleanEnum result = redisApprovedCacheUtils.getApprovedArg(joinPoint, annotation);
        assertEquals(BooleanEnum.FALSE, result);
    }

    @Test
    void returnsEvaluatedBooleanEnumWhenSpelExpressionIsProvided() {
        String spelExpression = "approved";

        Mockito.when(annotation.approvedArgumentPath()).thenReturn(spelExpression);
        Mockito.when(aspectUtils.evaluateSpelExpression(spelExpression, joinPoint)).thenReturn(true);

        BooleanEnum result = redisApprovedCacheUtils.getApprovedArg(joinPoint, annotation);
        assertEquals(BooleanEnum.TRUE, result);
    }

    @Test
    void returnsApprovedKeyBasedOnBooleanEnumValue() {
        String expected = ":approved:" + BooleanEnum.TRUE.getValue();
        String actual = redisApprovedCacheUtils.getApprovedKey(BooleanEnum.TRUE);
        assertEquals(expected, actual);
    }

    @Test
    void returnsForWhomKeyForGivenForWhomId() {
        Long forWhomId = 42L;
        String expected = ":forWhom:42";
        String actual = redisApprovedCacheUtils.getForWhomKey(forWhomId);
        assertEquals(expected, actual);
    }
}
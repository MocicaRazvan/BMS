package com.mocicarazvan.rediscache.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AspectUtilsTest {

    private AspectUtils aspectUtils;
    private ProceedingJoinPoint joinPoint;
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        aspectUtils = new AspectUtils();
        joinPoint = mock(ProceedingJoinPoint.class);
        methodSignature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
    }

    @Test
    void extractKeyFromAnnotation_withValidKeyPath_returnsKey() {
        String keyPath = "validKey";
        String result = aspectUtils.extractKeyFromAnnotation(keyPath, joinPoint);
        assertEquals(keyPath, result);
    }

    @Test
    void extractKeyFromAnnotation_withSpelExpression_returnsEvaluatedKey() {
        String keyPath = "#root";
        Object rootObjectValue = "rootObjectValue";

        MethodSignature methodSignature = mock(MethodSignature.class);
        when(methodSignature.getParameterNames()).thenReturn(new String[0]);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getTarget()).thenReturn(rootObjectValue);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);


        String result = aspectUtils.extractKeyFromAnnotation(keyPath, joinPoint);


        assertEquals("rootObjectValue", result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void extractKeyFromAnnotation_invalidKey_throwsException(String keyPath) {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                aspectUtils.extractKeyFromAnnotation(keyPath, joinPoint)
        );

        assertEquals("RedisReactiveCache annotation missing key", ex.getMessage());
    }

    @Test
    void getKeyVal_withUseArgsHashTrue_returnsKeyWithArgsHash() {
        String key = "key";
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        String result = aspectUtils.getKeyVal(joinPoint, key, true);
        assertTrue(result.startsWith("key_"));
    }

    @Test
    void getKeyVal_withUseArgsHashFalse_returnsKey() {
        String key = "key";
        String result = aspectUtils.getKeyVal(joinPoint, key, false);
        assertEquals("key", result);
    }

    @Test
    void assertString_withNonNullValue_returnsString() {
        Object value = "value";
        String result = aspectUtils.assertString(value);
        assertEquals("value", result);
    }

    @Test
    void assertString_withNullValue_throwsException() {
        assertThrows(RuntimeException.class, () -> aspectUtils.assertString(null));
    }

    @Test
    void assertLong_withValidLong_returnsLong() {
        Object value = 123L;
        Long result = aspectUtils.assertLong(value);
        assertEquals(123L, result);
    }

    @Test
    void assertLong_withValidString_returnsLong() {
        Object value = "123";
        Long result = aspectUtils.assertLong(value);
        assertEquals(123L, result);
    }

    @Test
    void assertLong_withInvalidString_throwsException() {
        Object value = "invalid";
        assertThrows(RuntimeException.class, () -> aspectUtils.assertLong(value));
    }

    @Test
    void appendStars_returnsStringWithStars() {
        String key = "key";
        String result = aspectUtils.appendStars(key);
        assertEquals("*key*", result);
    }

    @Test
    void evaluateSpelExpressionForObject_withValidExpression_returnsValue() {
        String spelExpression = "#param";
        when(joinPoint.getArgs()).thenReturn(new Object[]{"value"});
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"param"});
        Object result = aspectUtils.evaluateSpelExpressionForObject(spelExpression, "rootObject", joinPoint);
        assertEquals("value", result);
    }

    @Test
    void getHashString_returnsHashString() {
        String key = "key";
        String methodName = "methodName";
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        String result = aspectUtils.getHashString(joinPoint, key, methodName);
        assertNotNull(result);
    }

    @Test
    void getMethod_returnsMethod() throws NoSuchMethodException {
        Method method = AspectUtils.class.getMethod("getMethod", JoinPoint.class);
        when(methodSignature.getMethod()).thenReturn(method);
        Method result = aspectUtils.getMethod(joinPoint);
        assertEquals(method, result);
    }

    @Test
    void getTypeReference_returnsTypeReference() throws NoSuchMethodException {
        Method method = AspectUtils.class.getMethod("getMethod", JoinPoint.class);
        TypeReference<?> result = aspectUtils.getTypeReference(method);
        assertNotNull(result);
    }

    @Test
    void validateReturnTypeIsMonoPairClass_withValidMethod_doesNotThrowException() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("validMethod");
        assertDoesNotThrow(() -> aspectUtils.validateReturnTypeIsMonoPairClass(method, String.class));
    }

    @Test
    void validateReturnTypeIsMonoPairClass_withInvalidMethod_throwsException() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("invalidMethod");
        assertThrows(RuntimeException.class, () -> aspectUtils.validateReturnTypeIsMonoPairClass(method, String.class));
    }

    @Test
    void extractKeyFromAnnotation_withNullKeyPath_throwsException() {
        assertThrows(RuntimeException.class, () -> aspectUtils.extractKeyFromAnnotation(null, joinPoint));
    }

    @Test
    void extractKeyFromAnnotation_withEmptyKeyPath_throwsException() {
        assertThrows(RuntimeException.class, () -> aspectUtils.extractKeyFromAnnotation("", joinPoint));
    }

    @Test
    void getKeyVal_withNullKey_throwsException() {
        assertThrows(RuntimeException.class, () -> aspectUtils.getKeyVal(joinPoint, null, false));
    }

    @Test
    void getKeyVal_withEmptyKey_throwsException() {
        assertThrows(RuntimeException.class, () -> aspectUtils.getKeyVal(joinPoint, "", false));
    }

    @Test
    void assertLong_withNullValueAndDefault_returnsDefault() {
        Long defaultValue = 456L;
        Long result = aspectUtils.assertLong(null, defaultValue);
        assertEquals(defaultValue, result);
    }

    @Test
    void assertLong_withInvalidStringAndDefault_returnsDefault() {
        Long defaultValue = 456L;
        Long result = aspectUtils.assertLong("invalid", defaultValue);
        assertEquals(defaultValue, result);
    }

    @Test
    void evaluateSpelExpressionForObject_withNullExpression_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> aspectUtils.evaluateSpelExpressionForObject(null, "rootObject", joinPoint));
    }

    @Test
    void evaluateSpelExpressionForObject_withEmptyExpression_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> aspectUtils.evaluateSpelExpressionForObject("", "rootObject", joinPoint));
    }

    @Test
    void getHashString_withNullArgs_returnsHashString() {
        when(joinPoint.getArgs()).thenReturn(null);
        String result = aspectUtils.getHashString(joinPoint, "key", "methodName");
        assertNotNull(result);
    }

    @Test
    void getMethod_withNullSignature_throwsException() {
        when(joinPoint.getSignature()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> aspectUtils.getMethod(joinPoint));
    }

    static class TestClass {
        public Mono<Pair<String, String>> validMethod() {
            return Mono.just(Pair.of("key", "value"));
        }

        public String invalidMethod() {
            return "invalid";
        }
    }
}
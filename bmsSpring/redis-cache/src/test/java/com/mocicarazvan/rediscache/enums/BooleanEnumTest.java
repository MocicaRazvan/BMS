package com.mocicarazvan.rediscache.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BooleanEnumTest {

    @ParameterizedTest
    @MethodSource("provideFromBooleanTestData")
    void returnsExpectedBooleanEnumFromBoolean(Boolean input, BooleanEnum expected) {
        assertEquals(expected, BooleanEnum.fromBoolean(input));
    }

    static Stream<Arguments> provideFromBooleanTestData() {
        return Stream.of(
                Arguments.of(Boolean.TRUE, BooleanEnum.TRUE),
                Arguments.of(Boolean.FALSE, BooleanEnum.FALSE),
                Arguments.of(null, BooleanEnum.NULL)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFromStringTestData")
    void returnsExpectedBooleanEnumFromString(String input, BooleanEnum expected) {
        assertEquals(expected, BooleanEnum.fromString(input));
    }

    static Stream<Arguments> provideFromStringTestData() {
        return Stream.of(
                Arguments.of("TrUe", BooleanEnum.TRUE),
                Arguments.of(" FaLsE ", BooleanEnum.FALSE),
                Arguments.of(null, BooleanEnum.NULL)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFromObjectTestData")
    void returnsExpectedBooleanEnumFromObject(Object input, BooleanEnum expected) {
        assertEquals(expected, BooleanEnum.fromObject(input));
    }

    static Stream<Arguments> provideFromObjectTestData() {
        return Stream.of(
                Arguments.of(Boolean.TRUE, BooleanEnum.TRUE),
                Arguments.of("false", BooleanEnum.FALSE),
                Arguments.of(null, BooleanEnum.NULL)

        );
    }

    @ParameterizedTest
    @MethodSource("provideToBooleanTestData")
    void returnsExpectedBooleanFromToBoolean(BooleanEnum input, Boolean expected) {
        assertEquals(expected, BooleanEnum.toBoolean(input));
    }

    static Stream<Arguments> provideToBooleanTestData() {
        return Stream.of(
                Arguments.of(BooleanEnum.TRUE, Boolean.TRUE),
                Arguments.of(BooleanEnum.FALSE, Boolean.FALSE),
                Arguments.of(BooleanEnum.NULL, null)
        );
    }

    @Test
    void throwsExceptionForFromStringWhenInputIsInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> BooleanEnum.fromString("invalid"));
        assertEquals("Invalid value for BooleanEnum: invalid", exception.getMessage());
    }


    @Test
    void throwsExceptionForFromObjectWhenInputIsInvalidType() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> BooleanEnum.fromObject(123));
        assertTrue(exception.getMessage().contains("Invalid value for BooleanEnum:"));
    }


}
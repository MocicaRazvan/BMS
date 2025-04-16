package com.mocicarazvan.templatemodule.convertors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class BaseReadingConverterTest {

    private enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }

    private static class TestEnumConverter extends BaseReadingConverter<TestEnum> {
        public TestEnumConverter() {
            super(TestEnum.class);
        }
    }

    private final TestEnumConverter converter = new TestEnumConverter();

    @Test
    void convertValidEnumString() {
        TestEnum result = converter.convert("VALUE1");
        assertEquals(TestEnum.VALUE1, result);
    }

    @Test
    void convertNull() {
        TestEnum result = converter.convert(null);
        assertNull(result);
    }

    @Test
    void convertInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> {
            converter.convert("INVALID_VALUE");
        });
    }
}
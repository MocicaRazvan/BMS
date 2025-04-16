package com.mocicarazvan.templatemodule.convertors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
class BaseWritingConverterTest {
    private enum TestEnum {
        FIRST_VALUE,
        SECOND_VALUE,
        THIRD_VALUE
    }

    private static class TestEnumConverter extends BaseWritingConverter<TestEnum> {
        public TestEnumConverter() {
            super(TestEnum.class);
        }
    }

    private final TestEnumConverter converter = new TestEnumConverter();

    @Test
    void convertEnumToStringReturnsEnumName() {
        assertEquals("FIRST_VALUE", converter.convert(TestEnum.FIRST_VALUE));
        assertEquals("SECOND_VALUE", converter.convert(TestEnum.SECOND_VALUE));
        assertEquals("THIRD_VALUE", converter.convert(TestEnum.THIRD_VALUE));
    }

    @Test
    void convertNullReturnsNull() {
        assertNull(converter.convert(null));
    }
}
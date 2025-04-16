package com.mocicarazvan.ollamasearch.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
class TextPreprocessorTest {
    @Test
    void preprocessReturnsEmptyStringForNullInput() {
        assertEquals("", TextPreprocessor.preprocess((String) null));
    }

    @Test
    void preprocessReturnsEmptyStringForBlankInput() {
        assertEquals("", TextPreprocessor.preprocess("   "));
    }

    @Test
    void preprocessNormalizesAndTrimsText() {
        assertEquals("hello world", TextPreprocessor.preprocess("  Héllo   Wörld  "));
    }

    @Test
    void preprocessRemovesControlCharacters() {
        assertEquals("hello", TextPreprocessor.preprocess("he\u0000llo"));
    }

    @Test
    void preprocessHandlesEmptyList() {
        assertArrayEquals(new String[]{}, TextPreprocessor.preprocess(List.of()));
    }

    @Test
    void preprocessHandlesListWithNullAndBlankStrings() {
        assertArrayEquals(new String[]{"", "hello world"}, TextPreprocessor.preprocess(List.of("", "  Héllo   Wörld  ")));
    }

    @Test
    void preprocessHandlesEmptyArray() {
        assertArrayEquals(new String[]{}, TextPreprocessor.preprocess(new String[]{}));
    }

    @Test
    void preprocessHandlesArrayWithNullAndBlankStrings() {
        assertArrayEquals(new String[]{"", "hello world"}, TextPreprocessor.preprocess(new String[]{null, "  Héllo   Wörld  "}));
    }
}
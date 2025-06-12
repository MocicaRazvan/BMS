package com.mocicarazvan.nextstaticserver.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeaderSanitizerTest {
    @Test
    void sanitizeReturnsEmptyStringForNullInput() {
        Assertions.assertThat(HeaderSanitizer.sanitize(null)).isEmpty();
    }

    @Test
    void sanitizeReturnsOriginalStringWhenNoIllegalCharactersPresent() {
        String input = "Normal-Header_123";
        assertEquals(input, HeaderSanitizer.sanitize(input));
    }

    @Test
    void sanitizeRemovesControlCharactersFromString() {
        String input = "Valid\u0001Text\u001FHere";
        assertEquals("ValidTextHere", HeaderSanitizer.sanitize(input));
    }

    @Test
    void sanitizeRemovesDeleteCharacter() {
        String input = "Delete\u007FChar";
        assertEquals("DeleteChar", HeaderSanitizer.sanitize(input));
    }

    @Test
    void sanitizeReturnsEmptyStringWhenOnlyIllegalCharacters() {
        String input = "\u0000\u0010\u007F";
        assertTrue(HeaderSanitizer.sanitize(input).isEmpty());
    }
}
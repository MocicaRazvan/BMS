package com.mocicarazvan.ollamasearch.utils;

import com.mocicarazvan.ollamasearch.config.OllamaPropertiesConfig;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaQueryUtilsTest {

    @Mock
    private OllamaPropertiesConfig ollamaPropertiesConfig;
    @InjectMocks
    private OllamaQueryUtils ollamaQueryUtils;


    @Test
    @DisplayName("returns formatted string for valid embeddings")
    void returnsFormattedStringForValidEmbeddings() {
        OllamaEmbedResponseModel responseModel = mock(OllamaEmbedResponseModel.class);
        List<List<Double>> mockEmbeddings = List.of(List.of(1.23, 4.56, 7.89));

        when(responseModel.getEmbeddings()).thenReturn(mockEmbeddings);

        String result = ollamaQueryUtils.getEmbeddingsAsString(responseModel);

        assertEquals("[1.23, 4.56, 7.89]", result);
    }

    @Test
    @DisplayName("throws NullPointerException when embeddings are null")
    void throwsExceptionWhenEmbeddingsAreNull() {
        OllamaEmbedResponseModel responseModel = mock(OllamaEmbedResponseModel.class);
        when(responseModel.getEmbeddings()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> ollamaQueryUtils.getEmbeddingsAsString(responseModel));
    }


    @Test
    @DisplayName("returns formatted string for valid vector")
    void returnsFormattedStringForValidVector() {
        String result = ollamaQueryUtils.addOrder("vector");

        assertEquals("(e.embedding <#> 'vector')", result);
    }

    @Test
    @DisplayName("returns empty string for null vector")
    void returnsEmptyStringForNullVector() {
        String result = ollamaQueryUtils.addOrder(null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("returns empty string for empty vector")
    void returnsEmptyStringForEmptyVector() {
        String result = ollamaQueryUtils.addOrder("");

        assertEquals("", result);
    }


    @Test
    @DisplayName("returns formatted string for valid vector and threshold")
    void returnsFormattedStringForValidVectorAndThreshold() {
        when(ollamaPropertiesConfig.getThreshold()).thenReturn(0.5);

        String result = ollamaQueryUtils.addThresholdFilter("vector");

        assertEquals("(e.embedding <#> 'vector') * -1 >= 0.5", result);
    }

    @Test
    @DisplayName("returns empty string for null vector for threshold")
    void returnsEmptyStringForNullVectorThreshold() {
        String result = ollamaQueryUtils.addThresholdFilter(null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("returns formatted string with extra filter")
    void returnsFormattedStringWithExtraFilter() {
        when(ollamaPropertiesConfig.getThreshold()).thenReturn(0.5);

        String result = ollamaQueryUtils.addThresholdFilter("vector", "AND e.id = 1");

        assertEquals("( (e.embedding <#> 'vector') * -1 >= 0.5 AND e.id = 1 )", result);
    }


    @Test
    @DisplayName("returns true for null input")
    void returnsTrueForNullInput() {
        assertTrue(ollamaQueryUtils.isNullOrEmpty(null));
    }

    @Test
    @DisplayName("returns true for empty string")
    void returnsTrueForEmptyString() {
        assertTrue(ollamaQueryUtils.isNullOrEmpty(""));
    }

    @Test
    @DisplayName("returns true for blank string")
    void returnsTrueForBlankString() {
        assertTrue(ollamaQueryUtils.isNullOrEmpty("   "));
    }

    @Test
    @DisplayName("returns false for non-empty string")
    void returnsFalseForNonEmptyString() {
        assertFalse(ollamaQueryUtils.isNullOrEmpty("value"));
    }

    @Test
    @DisplayName("add distance vector not null or empty")
    void addDistanceFilter() {
        String result = ollamaQueryUtils.addDistance("vector");
        assertEquals("(e.embedding <#> 'vector') * -1 AS distance", result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("add distance vector null or empty")
    void addDistanceFilterNullOrEmpty(String vector) {
        String result = ollamaQueryUtils.addDistance(vector);
        assertEquals("", result);
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", " \r\n "})
    @DisplayName("is not null or empty true")
    void isNotNullOrEmptyTrue(String input) {
        assertTrue(ollamaQueryUtils.isNullOrEmpty(input));
    }

    @ParameterizedTest
    @ValueSource(strings =
            {"a", "abc", "123", "abc123", "a b c"})
    @DisplayName("is not null or empty false")
    void isNotNullOrEmptyFalse(String input) {
        assertFalse(ollamaQueryUtils.isNullOrEmpty(input));
    }


}
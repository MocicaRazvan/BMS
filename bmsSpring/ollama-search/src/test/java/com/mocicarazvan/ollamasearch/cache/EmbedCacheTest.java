package com.mocicarazvan.ollamasearch.cache;

import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.function.Function;


class EmbedCacheTest {

    private final EmbedCache embedCache = new EmbedCache() {
        @Override
        public Mono<OllamaEmbedResponseModel> getEmbedding(String text, Function<String, Mono<OllamaEmbedResponseModel>> cacheMissFunction) {
            return cacheMissFunction.apply(text);
        }
    };

    @Test
    @DisplayName("getEmbeddingKey should generate a valid key for normal input")
    void getEmbeddingKeyGeneratesValidKeyForNormalInput() {
        String key = embedCache.getEmbeddingKey("key", "Some Text");
        assert key.equals("key:some_text");
    }

    @Test
    @DisplayName("getEmbeddingKey should handle empty text")
    void getEmbeddingKeyHandlesEmptyText() {
        String key = embedCache.getEmbeddingKey("key", "");
        assert key.equals("key:");
    }

    @Test
    @DisplayName("getEmbeddingKey should handle special characters in text")
    void getEmbeddingKeyHandlesSpecialCharacters() {
        String key = embedCache.getEmbeddingKey("key", "Text!@#$%^&*()");
        assert key.equals("key:text__________");
    }


}
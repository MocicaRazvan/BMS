package com.mocicarazvan.ollamasearch.cache;


import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface EmbedCache {

    default String getEmbeddingKey(String key, String text) {
        return key + ":" + text.trim().toLowerCase().replaceAll("\\s+", " ").replaceAll("[^a-z0-9_-]", "_");
    }

    Mono<OllamaEmbedResponseModel> getEmbedding(String text, Function<String, OllamaEmbedResponseModel> cacheMissFunction);
}

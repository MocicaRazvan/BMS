package com.mocicarazvan.ollamasearch.cache;


import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedResponseModel;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface EmbedCache {

    default String getEmbeddingKey(String key, String text) {
        return key + ":" + text.toLowerCase().replaceAll("\\s+", " ").trim().replaceAll("[^a-z0-9_-]", "_");
    }

    Mono<OllamaEmbedResponseModel> getEmbedding(String text, Function<String, Mono<OllamaEmbedResponseModel>> cacheMissFunction);
}

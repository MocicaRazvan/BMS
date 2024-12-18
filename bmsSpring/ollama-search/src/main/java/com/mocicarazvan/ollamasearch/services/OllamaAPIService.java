package com.mocicarazvan.ollamasearch.services;

import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OllamaAPIService {

    OllamaEmbedResponseModel generateEmbedding(String text);

    OllamaEmbedResponseModel generateEmbeddings(List<String> texts);

    float[] generateEmbeddingFloat(String text);

    Mono<OllamaEmbedResponseModel> generateEmbeddingMono(String text, EmbedCache embedCache);

    Mono<Float[]> generateEmbeddingFloatMono(String text, EmbedCache embedCache);

    float[] convertToFloatPrimitive(Float[] floats);

    Mono<String> getEmbedding(String text, EmbedCache embedCache);

    boolean isNotNullOrEmpty(String text);
}

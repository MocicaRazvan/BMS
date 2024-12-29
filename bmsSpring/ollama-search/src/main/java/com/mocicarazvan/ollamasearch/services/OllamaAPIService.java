package com.mocicarazvan.ollamasearch.services;

import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import com.mocicarazvan.ollamasearch.dtos.embed.OllamaEmbedResponseModel;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OllamaAPIService {

    Mono<OllamaEmbedResponseModel> generateEmbedding(String text);

    Mono<OllamaEmbedResponseModel> generateEmbeddings(List<String> texts);

    Mono<OllamaEmbedResponseModel> generateEmbeddingWithCache(String text, EmbedCache embedCache);

    Mono<Float[]> generateEmbeddingFloatMonoWithCache(String text, EmbedCache embedCache);

    Mono<Float[]> generateEmbeddingFloatMono(String text);

    float[] convertToFloatPrimitive(Float[] floats);

    Mono<String> getEmbedding(String text, EmbedCache embedCache);

    boolean isNotNullOrEmpty(String text);
}

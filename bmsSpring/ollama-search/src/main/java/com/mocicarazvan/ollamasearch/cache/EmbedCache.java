package com.mocicarazvan.ollamasearch.cache;


import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@FunctionalInterface
public interface EmbedCache {

    Mono<OllamaEmbedResponseModel> getEmbedding(String text, Function<String, OllamaEmbedResponseModel> cacheMissFunction);
}

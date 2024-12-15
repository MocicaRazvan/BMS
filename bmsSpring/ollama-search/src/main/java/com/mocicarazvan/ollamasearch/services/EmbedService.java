package com.mocicarazvan.ollamasearch.services;

import com.mocicarazvan.ollamasearch.models.EmbedModel;
import reactor.core.publisher.Mono;

public interface EmbedService<T extends EmbedModel> {

    Mono<T> saveEmbedding(Long entityId, String embedText);

    Mono<Void> deleteEmbedding(Long entityId);

    <R> Mono<R> updateEmbeddingWithZip(String newText, String oldText, Long entityId, Mono<R> mono);
}

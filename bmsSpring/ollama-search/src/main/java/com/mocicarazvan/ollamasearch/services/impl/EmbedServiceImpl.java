package com.mocicarazvan.ollamasearch.services.impl;

import com.mocicarazvan.ollamasearch.models.EmbedModel;
import com.mocicarazvan.ollamasearch.repositories.EmbedModelRepository;
import com.mocicarazvan.ollamasearch.services.EmbedService;
import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
public abstract class EmbedServiceImpl<T extends EmbedModel, M extends EmbedModelRepository<T>> implements EmbedService<T> {

    private final OllamaAPIService ollamaAPIService;
    private final TransactionalOperator transactionalOperator;
    private final M embedRepository;

    protected abstract T createEmbedding();

    @Override
    public Mono<T> saveEmbedding(Long entityId, String embedText) {
        T t = createEmbedding();
        t.setEntityId(entityId);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        return ollamaAPIService.generateEmbeddingFloatMono(embedText)
                .flatMap(embeddings -> {
                    t.setEmbedding(ollamaAPIService.convertToFloatPrimitive(embeddings));
                    return embedRepository.save(t);
                });

    }

    @Override
    public Mono<Void> deleteEmbedding(Long entityId) {
        return embedRepository.deleteByEntityId(entityId);
    }

    @Override
    public <R> Mono<R> updateEmbeddingWithZip(String newText, String oldText, Long entityId, Mono<R> mono) {
        if (!Objects.equals(newText, oldText)) {
            return embedRepository.deleteByEntityId(entityId)
                    .then(Mono.zip(
                            Mono.defer(() ->
                                    saveEmbedding(entityId, newText)),
                            Mono.defer(() -> mono)
                    ).map(Tuple2::getT2)).as(transactionalOperator::transactional);
        }
        return mono;
    }
}

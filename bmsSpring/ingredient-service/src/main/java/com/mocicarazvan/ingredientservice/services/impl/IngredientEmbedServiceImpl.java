package com.mocicarazvan.ingredientservice.services.impl;


import com.mocicarazvan.ingredientservice.models.IngredientEmbedding;
import com.mocicarazvan.ingredientservice.repositories.IngredientEmbeddingRepository;
import com.mocicarazvan.ollamasearch.service.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.EmbedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IngredientEmbedServiceImpl implements EmbedService<IngredientEmbedding> {

    private final OllamaAPIService ollamaAPIService;
    private final IngredientEmbeddingRepository ingredientEmbeddingRepository;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<IngredientEmbedding> saveEmbedding(Long entityId, String embedText) {
        return ingredientEmbeddingRepository.save(
                IngredientEmbedding.builder()
                        .entityId(entityId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .embedding(ollamaAPIService.generateEmbeddingFloat(embedText))
                        .build());
    }


    @Override
    public Mono<Void> deleteEmbedding(Long entityId) {
        return ingredientEmbeddingRepository.deleteByEntityId(entityId);
    }

    @Override
    public <R> Mono<R> updateEmbeddingWithZip(String newText, String oldText, Long entityId, Mono<R> mono) {
        if (!Objects.equals(newText, oldText)) {
            return ingredientEmbeddingRepository.deleteByEntityId(entityId)
                    .then(Mono.zip(
                            Mono.defer(() ->
                                    ingredientEmbeddingRepository.save(IngredientEmbedding.builder()
                                            .entityId(entityId)
                                            .embedding(ollamaAPIService.generateEmbeddingFloat(newText))
                                            .updatedAt(LocalDateTime.now())
                                            .build())),
                            Mono.defer(() -> mono)

                    ).map(Tuple2::getT2)).as(transactionalOperator::transactional);
        }
        return mono;
    }
}

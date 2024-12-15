package com.mocicarazvan.recipeservice.services.impl;


import com.mocicarazvan.ollamasearch.service.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.EmbedService;
import com.mocicarazvan.recipeservice.models.RecipeEmbedding;
import com.mocicarazvan.recipeservice.repositories.RecipeEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RecipeEmbedServiceImpl implements EmbedService<RecipeEmbedding> {

    private final OllamaAPIService ollamaAPIService;
    private final RecipeEmbeddingRepository recipeEmbeddingRepository;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<RecipeEmbedding> saveEmbedding(Long entityId, String embedText) {
        return recipeEmbeddingRepository.save(
                RecipeEmbedding.builder()
                        .entityId(entityId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .embedding(ollamaAPIService.generateEmbeddingFloat(embedText))
                        .build());
    }


    @Override
    public Mono<Void> deleteEmbedding(Long entityId) {
        return recipeEmbeddingRepository.deleteByEntityId(entityId);
    }

    @Override
    public <R> Mono<R> updateEmbeddingWithZip(String newText, String oldText, Long entityId, Mono<R> mono) {
        if (!Objects.equals(newText, oldText)) {
            return recipeEmbeddingRepository.deleteByEntityId(entityId)
                    .then(Mono.zip(
                            Mono.defer(() ->
                                    recipeEmbeddingRepository.save(RecipeEmbedding.builder()
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

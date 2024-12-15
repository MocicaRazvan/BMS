package com.mocicarazvan.planservice.services.impl;


import com.mocicarazvan.ollamasearch.service.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.EmbedService;
import com.mocicarazvan.planservice.models.PlanEmbedding;
import com.mocicarazvan.planservice.repositories.PlanEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PlanEmbedServiceImpl implements EmbedService<PlanEmbedding> {

    private final OllamaAPIService ollamaAPIService;
    private final PlanEmbeddingRepository planEmbeddingRepository;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<PlanEmbedding> saveEmbedding(Long entityId, String embedText) {
        return planEmbeddingRepository.save(
                PlanEmbedding.builder()
                        .entityId(entityId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .embedding(ollamaAPIService.generateEmbeddingFloat(embedText))
                        .build());
    }


    @Override
    public Mono<Void> deleteEmbedding(Long entityId) {
        return planEmbeddingRepository.deleteByEntityId(entityId);
    }

    @Override
    public <R> Mono<R> updateEmbeddingWithZip(String newText, String oldText, Long entityId, Mono<R> mono) {
        if (!Objects.equals(newText, oldText)) {
            return planEmbeddingRepository.deleteByEntityId(entityId)
                    .then(Mono.zip(
                            Mono.defer(() ->
                                    planEmbeddingRepository.save(PlanEmbedding.builder()
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

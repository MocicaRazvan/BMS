package com.mocicarazvan.dayservice.services.impl;

import com.mocicarazvan.dayservice.models.DayEmbedding;
import com.mocicarazvan.dayservice.repositories.DayEmbeddingRepository;
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
public class DayEmbedServiceImpl implements EmbedService<DayEmbedding> {

    private final OllamaAPIService ollamaAPIService;
    private final DayEmbeddingRepository dayEmbeddingRepository;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<DayEmbedding> saveEmbedding(Long entityId, String embedText) {
        return dayEmbeddingRepository.save(
                DayEmbedding.builder()
                        .entityId(entityId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .embedding(ollamaAPIService.generateEmbeddingFloat(embedText))
                        .build());
    }


    @Override
    public Mono<Void> deleteEmbedding(Long entityId) {
        return dayEmbeddingRepository.deleteByEntityId(entityId);
    }

    @Override
    public <R> Mono<R> updateEmbeddingWithZip(String newText, String oldText, Long entityId, Mono<R> mono) {
        if (!Objects.equals(newText, oldText)) {
            return dayEmbeddingRepository.deleteByEntityId(entityId)
                    .then(Mono.zip(
                            Mono.defer(() ->
                                    dayEmbeddingRepository.save(DayEmbedding.builder()
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

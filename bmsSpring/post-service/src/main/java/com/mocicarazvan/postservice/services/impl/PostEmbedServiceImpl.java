package com.mocicarazvan.postservice.services.impl;


import com.mocicarazvan.ollamasearch.service.OllamaAPIService;
import com.mocicarazvan.ollamasearch.services.EmbedService;
import com.mocicarazvan.postservice.models.PostEmbedding;
import com.mocicarazvan.postservice.repositories.PostEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostEmbedServiceImpl implements EmbedService<PostEmbedding> {

    private final OllamaAPIService ollamaAPIService;
    private final PostEmbeddingRepository postEmbeddingRepository;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<PostEmbedding> saveEmbedding(Long entityId, String embedText) {
        return postEmbeddingRepository.save(
                PostEmbedding.builder()
                        .entityId(entityId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .embedding(ollamaAPIService.generateEmbeddingFloat(embedText))
                        .build());
    }


    @Override
    public Mono<Void> deleteEmbedding(Long entityId) {
        return postEmbeddingRepository.deleteByEntityId(entityId);
    }

    @Override
    public <R> Mono<R> updateEmbeddingWithZip(String newText, String oldText, Long entityId, Mono<R> mono) {
        if (!Objects.equals(newText, oldText)) {
            return postEmbeddingRepository.deleteByEntityId(entityId)
                    .then(Mono.zip(
                            Mono.defer(() ->
                                    postEmbeddingRepository.save(PostEmbedding.builder()
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

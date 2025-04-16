package com.mocicarazvan.ollamasearch.services;

import com.mocicarazvan.ollamasearch.impl.EmbedModelImpl;
import com.mocicarazvan.ollamasearch.impl.EmbedModelRepositoryTestImpl;
import com.mocicarazvan.ollamasearch.impl.EmbedServiceTestImpl;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class EmbedServiceTest {

    @Mock
    private OllamaAPIService ollamaAPIService;

    @Mock
    private TransactionalOperator transactionalOperator;

    @Mock
    private EmbedModelRepositoryTestImpl repository;

    @InjectMocks
    private EmbedServiceTestImpl service;

    private final Float[] embedding = new Float[]{0.1f, 0.2f, 0.3f};
    private final float[] prmitiveEmbedding = new float[]{0.1f, 0.2f, 0.3f};

    @Test
    @Order(1)
    void loads() {
        assertNotNull(service);
    }

    @Test
    void saveEmbedding() {
        var text = "test";
        var entityId = 1L;
        when(ollamaAPIService.generateEmbeddingFloatMono(text)).thenReturn(Mono.just(embedding));
        when(ollamaAPIService.convertToFloatPrimitive(embedding)).thenReturn(prmitiveEmbedding);
        when(repository.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(service.saveEmbedding(entityId, text))
                .assertNext(resp -> {
                    assertEquals(entityId, resp.getEntityId());
                    assertArrayEquals(prmitiveEmbedding, resp.getEmbedding());
                })
                .verifyComplete();
    }

    @Test
    void deleteEmbedding() {
        var entityId = 1L;
        when(repository.deleteByEntityId(entityId)).thenReturn(Mono.empty());
        StepVerifier.create(service.deleteEmbedding(entityId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void updateEmbeddingWithZip_sameNewAndOld() {
        var text = "test";
        var pT = "publishedText";
        Mono<String> innerMono = Mono.just(pT);

        StepVerifier.create(service.updateEmbeddingWithZip(text, text, 1L, innerMono))
                .expectNext(pT)
                .verifyComplete();

    }

    @Test
    void updateEmbeddingWithZip_differentNewAndOld() {
        var newText = "newText";
        var oldText = "oldText";
        var pT = "publishedText";
        var entityId = 1L;
        Mono<String> innerMono = Mono.just(pT);
        when(repository.deleteByEntityId(entityId)).thenReturn(Mono.empty());
        when(ollamaAPIService.generateEmbeddingFloatMono(newText)).thenReturn(Mono.just(embedding));
        when(ollamaAPIService.convertToFloatPrimitive(embedding)).thenReturn(prmitiveEmbedding);
        when(repository.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(i -> i.getArgument(0));


        StepVerifier.create(service.updateEmbeddingWithZip(newText, oldText, entityId, innerMono))
                .expectNext(pT)
                .verifyComplete();

        var argumentCaptor = ArgumentCaptor.forClass(EmbedModelImpl.class);
        verify(repository, times(1)).save(argumentCaptor.capture());

        var savedEntity = argumentCaptor.getValue();
        assertEquals(entityId, savedEntity.getEntityId());
        assertArrayEquals(prmitiveEmbedding, savedEntity.getEmbedding());

    }
}
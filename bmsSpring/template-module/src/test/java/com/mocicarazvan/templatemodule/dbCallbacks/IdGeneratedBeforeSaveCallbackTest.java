package com.mocicarazvan.templatemodule.dbCallbacks;

import com.mocicarazvan.templatemodule.models.IdGeneratedImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

class IdGeneratedBeforeSaveCallbackTest {

    private IdGeneratedBeforeSaveCallback<IdGeneratedImpl> callback;

    @BeforeEach
    void setUp() {
        callback = new IdGeneratedBeforeSaveCallback<>();
    }


    @Test
    void shouldSetCreatedAtAndUpdatedAtWhenBothAreNull() {
        IdGeneratedImpl entity = new IdGeneratedImpl();
        StepVerifier.create(callback.onBeforeSave(entity, null, null))
                .expectNextMatches(e -> e.getCreatedAt() != null && e.getUpdatedAt() != null)
                .verifyComplete();
    }

    @Test
    void shouldNotOverwriteCreatedAtIfAlreadySet() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        IdGeneratedImpl entity = new IdGeneratedImpl();
        entity.setCreatedAt(createdAt);

        StepVerifier.create(callback.onBeforeSave(entity, null, null))
                .expectNextMatches(e -> e.getCreatedAt().equals(createdAt) && e.getUpdatedAt() != null)
                .verifyComplete();
    }

    @Test
    void shouldSetUpdatedAtEvenIfCreatedAtIsAlreadySet() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        IdGeneratedImpl entity = new IdGeneratedImpl();
        entity.setCreatedAt(createdAt);

        StepVerifier.create(callback.onBeforeSave(entity, null, null))
                .expectNextMatches(e -> e.getUpdatedAt() != null && e.getUpdatedAt().isAfter(createdAt))
                .verifyComplete();
    }

    @Test
    void shouldNotOverwriteUpdatedAtIfAlreadySet() {
        LocalDateTime updatedAt = LocalDateTime.now().minusHours(1);
        IdGeneratedImpl entity = new IdGeneratedImpl();
        entity.setUpdatedAt(updatedAt);

        StepVerifier.create(callback.onBeforeSave(entity, null, null))
                .expectNextMatches(e -> e.getUpdatedAt().equals(updatedAt))
                .verifyComplete();
    }

}
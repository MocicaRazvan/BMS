package com.mocicarazvan.ollamasearch.dbCallbacks;

import com.mocicarazvan.ollamasearch.impl.EmbedModelImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class EmbedModelBeforeSaveCallbackTest {
    private final EmbedModelBeforeSaveCallback<EmbedModelImpl> callback = new EmbedModelBeforeSaveCallback<>();
    @Mock
    private OutboundRow row;
    @Mock
    private SqlIdentifier table;


    @Test
    void setsCreatedAtAndUpdatedAtWhenCreatedAtIsNull() {
        EmbedModelImpl entity = new EmbedModelImpl();
        entity.setCreatedAt(null);


        StepVerifier.create(callback.onBeforeSave(entity, row, table))
                .expectNextMatches(e -> e.getCreatedAt() != null && e.getUpdatedAt() != null)
                .verifyComplete();
    }

    @Test
    void updatesUpdatedAtWhenCreatedAtIsNotNull() {
        EmbedModelImpl entity = new EmbedModelImpl();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        entity.setCreatedAt(createdAt);

        StepVerifier.create(callback.onBeforeSave(entity, row, table))
                .expectNextMatches(e -> e.getCreatedAt().equals(createdAt) && e.getUpdatedAt() != null)
                .verifyComplete();
    }

    @Test
    void doesNotModifyCreatedAtWhenAlreadySet() {
        EmbedModelImpl entity = new EmbedModelImpl();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        entity.setCreatedAt(createdAt);


        StepVerifier.create(callback.onBeforeSave(entity, row, table))
                .expectNextMatches(e -> e.getCreatedAt().equals(createdAt))
                .verifyComplete();
    }
}
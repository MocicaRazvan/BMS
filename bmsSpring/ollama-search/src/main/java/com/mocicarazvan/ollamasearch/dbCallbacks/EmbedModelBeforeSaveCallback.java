package com.mocicarazvan.ollamasearch.dbCallbacks;

import com.mocicarazvan.ollamasearch.models.EmbedModel;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public class EmbedModelBeforeSaveCallback<T extends EmbedModel> implements BeforeSaveCallback<T> {
    @Override
    public Publisher<T> onBeforeSave(T entity, OutboundRow row, SqlIdentifier table) {

        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }

        entity.setUpdatedAt(now);

        return Mono.just(entity);
    }
}

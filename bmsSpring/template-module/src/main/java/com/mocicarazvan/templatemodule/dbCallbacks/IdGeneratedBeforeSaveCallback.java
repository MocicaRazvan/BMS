package com.mocicarazvan.templatemodule.dbCallbacks;

import com.mocicarazvan.templatemodule.models.IdGenerated;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public class IdGeneratedBeforeSaveCallback<T extends IdGenerated> implements BeforeSaveCallback<T> {

    @Override
    public Publisher<T> onBeforeSave(T entity, OutboundRow row, SqlIdentifier table) {
        return Mono.just(handleBeforeSave(entity));
    }

    protected T handleBeforeSave(T entity) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        if (entity.getUpdatedAt() == null) {
            entity.setUpdatedAt(now);
        }
        return entity;
    }
}

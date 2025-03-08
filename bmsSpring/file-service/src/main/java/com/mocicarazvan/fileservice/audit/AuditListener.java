package com.mocicarazvan.fileservice.audit;

import com.mocicarazvan.fileservice.models.Auditable;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class AuditListener implements ReactiveBeforeConvertCallback<Auditable> {
    @Override
    public Mono<Auditable> onBeforeConvert(Auditable entity, String collection) {
        Instant now = Instant.now();

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);

        return Mono.just(entity);
    }


}

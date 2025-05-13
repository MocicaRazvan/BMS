package com.mocicarazvan.websocketservice.hibernate;

import com.mocicarazvan.websocketservice.models.generic.IdGenerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

public class IdGeneratedListener {
    @PrePersist
    @PreUpdate
    public void prePersist(IdGenerated entity) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);
    }
}



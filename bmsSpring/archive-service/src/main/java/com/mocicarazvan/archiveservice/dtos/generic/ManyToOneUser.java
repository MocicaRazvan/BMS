package com.mocicarazvan.archiveservice.dtos.generic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class ManyToOneUser extends IdGenerated {

    private Long userId;

    @Override
    public String toString() {
        return "ManyToOneUser{" +
                "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "userId=" + userId +
                '}';
    }
}

package com.mocicarazvan.templatemodule.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder
@Table("id_generated")
public class IdGeneratedImpl extends IdGenerated {

    public boolean equalsIgnoreId(IdGeneratedImpl other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        return getCreatedAt().equals(other.getCreatedAt()) && getUpdatedAt().equals(other.getUpdatedAt());
    }
}

package com.mocicarazvan.templatemodule.models;

import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@Table("associative_entity")
@SuperBuilder
public class AssociativeEntityImpl extends AssociativeEntity {

    @Override
    public String toString() {
        return "AssociativeEntityImpl{" +
                "masterId=" + getMasterId() +
                ", childId=" + getChildId() +
                '}';
    }
}

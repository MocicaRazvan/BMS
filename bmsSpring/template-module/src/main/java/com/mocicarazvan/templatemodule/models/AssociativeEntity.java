package com.mocicarazvan.templatemodule.models;

import com.mocicarazvan.templatemodule.models.keys.AssociativeEntityKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AssociativeEntity extends AssociativeEntityKey implements Cloneable,
        Persistable<AssociativeEntityKey> {

    private Long multiplicity;


    @Override
    public AssociativeEntity clone() {
        try {
            return (AssociativeEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public AssociativeEntityKey getId() {
        return AssociativeEntityKey
                .builder()
                .masterId(getMasterId())
                .childId(getChildId())
                .build();
    }

    @Override
    public boolean isNew() {
        return true;
    }
}

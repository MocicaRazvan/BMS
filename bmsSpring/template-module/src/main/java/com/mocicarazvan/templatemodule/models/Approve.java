package com.mocicarazvan.templatemodule.models;

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
public abstract class Approve extends TitleBodyImages implements Cloneable {
    private boolean approved;

    @Override
    public Approve clone() {
        return (Approve) super.clone();
    }
}

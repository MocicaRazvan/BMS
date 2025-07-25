package com.mocicarazvan.templatemodule.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class ManyToOneUser extends IdGenerated implements Cloneable {
    @Column("user_id")
    private Long userId;

    @Override
    public ManyToOneUser clone() {
        return (ManyToOneUser) super.clone();
    }
}

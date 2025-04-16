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
@Table("many_to_one_user")
public class ManyToOneUserImpl extends ManyToOneUser {

    @Override
    public ManyToOneUserImpl clone() {
        return (ManyToOneUserImpl) super.clone();
    }
}

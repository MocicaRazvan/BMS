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
@Table("tile_body")
public class TitleBodyImpl extends TitleBody {

    @Override
    public TitleBodyImpl clone() {
        return (TitleBodyImpl) super.clone();
    }
}

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
@Table("tile_body_images")
public class TitleBodyImagesImpl extends TitleBodyImages {

    @Override
    public TitleBodyImagesImpl clone() {
        return (TitleBodyImagesImpl) super.clone();
    }
}

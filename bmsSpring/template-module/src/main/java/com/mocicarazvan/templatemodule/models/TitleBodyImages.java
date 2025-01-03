package com.mocicarazvan.templatemodule.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class TitleBodyImages extends TitleBody implements Cloneable {
    private List<String> images;

    @Override
    public TitleBodyImages clone() {
        TitleBodyImages clone = (TitleBodyImages) super.clone();
        clone.setImages(List.copyOf(images));
        return clone;
    }
}

package com.mocicarazvan.templatemodule.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        clone.setImages(new ArrayList<>(Optional.ofNullable(images).orElseGet(ArrayList::new)));
        return clone;
    }
}

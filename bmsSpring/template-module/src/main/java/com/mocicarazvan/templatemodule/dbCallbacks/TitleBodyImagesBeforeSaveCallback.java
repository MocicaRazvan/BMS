package com.mocicarazvan.templatemodule.dbCallbacks;

import com.mocicarazvan.templatemodule.models.TitleBodyImages;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;

@Order(Ordered.LOWEST_PRECEDENCE)
public class TitleBodyImagesBeforeSaveCallback<T extends TitleBodyImages> extends TitleBodyBeforeSaveCallback<T> {


    @Override
    protected T handleBeforeSave(T entity) {
        T e = super.handleBeforeSave(entity);
        if (e.getImages() == null) {
            e.setImages(new ArrayList<>());
        }
        return e;
    }
}

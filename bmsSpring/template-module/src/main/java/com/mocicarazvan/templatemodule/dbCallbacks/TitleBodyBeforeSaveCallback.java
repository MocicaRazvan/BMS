package com.mocicarazvan.templatemodule.dbCallbacks;

import com.mocicarazvan.templatemodule.models.TitleBody;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;

@Order(Ordered.LOWEST_PRECEDENCE)
public class TitleBodyBeforeSaveCallback<T extends TitleBody> extends IdGeneratedBeforeSaveCallback<T> {


    @Override
    protected T handleBeforeSave(T entity) {
        T e = super.handleBeforeSave(entity);
        if (e.getUserLikes() == null) {
            e.setUserLikes(new ArrayList<>());
        }

        if (e.getUserDislikes() == null) {
            e.setUserDislikes(new ArrayList<>());
        }

        e.setBody(e.getBody().strip());
        e.setTitle(e.getTitle().replaceAll("\\s+", " ").strip());

        return e;
    }
}

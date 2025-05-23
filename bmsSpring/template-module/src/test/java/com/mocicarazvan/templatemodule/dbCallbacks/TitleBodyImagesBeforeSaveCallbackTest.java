package com.mocicarazvan.templatemodule.dbCallbacks;


import com.mocicarazvan.templatemodule.models.TitleBodyImagesImpl;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

class TitleBodyImagesBeforeSaveCallbackTest {

    private final TitleBodyImagesBeforeSaveCallback<TitleBodyImagesImpl> callback = new TitleBodyImagesBeforeSaveCallback<>();

    @Test
    void shouldInitializeImagesWhenNull() {
        TitleBodyImagesImpl entity = new TitleBodyImagesImpl();
        entity.setBody("b");
        entity.setTitle("t");
        entity.setImages(null);

        StepVerifier.create(callback.onBeforeSave(entity, null, null))
                .expectNextMatches(e -> e.getImages() != null && e.getImages().isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldNotOverwriteImagesIfAlreadySet() {
        TitleBodyImagesImpl entity = new TitleBodyImagesImpl();
        entity.setBody("b");
        entity.setTitle("t");
        entity.setImages(new ArrayList<>(List.of("image1", "image2")));

        StepVerifier.create(callback.onBeforeSave(entity, null, null))
                .expectNextMatches(e -> e.getImages().size() == 2 && e.getImages().contains("image1"))
                .verifyComplete();
    }
}
package com.mocicarazvan.templatemodule.dbCallbacks;

import com.mocicarazvan.templatemodule.models.TitleBodyImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

class TitleBodyBeforeSaveCallbackTest {
    private final TitleBodyBeforeSaveCallback<TitleBodyImpl> callback = new TitleBodyBeforeSaveCallback<>();


    @Test
    @DisplayName("should initialize userLikes and userDislikes when both are null")
    void shouldInitializeUserLikesAndUserDislikesWhenBothAreNull() {
        TitleBodyImpl entity = new TitleBodyImpl();
        entity.setBody("  Sample body  ");
        entity.setTitle("  Sample   title  ");

        StepVerifier.create(callback.onBeforeSave(entity, null, null))
                .expectNextMatches(e -> e.getUserLikes() != null && e.getUserDislikes() != null)
                .verifyComplete();
    }

    @Test
    @DisplayName("should not overwrite userLikes and userDislikes if already set")
    void shouldNotOverwriteUserLikesAndUserDislikesIfAlreadySet() {
        TitleBodyImpl entity = new TitleBodyImpl();
        entity.setUserLikes(new ArrayList<>(List.of(1L)));
        entity.setUserDislikes(new ArrayList<>(List.of(2L)));
        entity.setBody("  Sample body  ");
        entity.setTitle("  Sample   title  ");

        StepVerifier.create(callback.onBeforeSave(entity, null, null))
                .expectNextMatches(e -> e.getUserLikes().size() == 1 && e.getUserDislikes().size() == 1)
                .verifyComplete();
    }

    @Test
    @DisplayName("should trim body and normalize title spacing")
    void shouldTrimBodyAndNormalizeTitleSpacing() {
        TitleBodyImpl entity = new TitleBodyImpl();
        entity.setBody("  Sample body  ");
        entity.setTitle("  Sample   title  ");

        StepVerifier.create(callback.onBeforeSave(entity, null, null))
                .expectNextMatches(e -> e.getBody().equals("Sample body") && e.getTitle().equals("Sample title"))
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle empty body and title gracefully")
    void shouldHandleEmptyBodyAndTitleGracefully() {
        TitleBodyImpl entity = new TitleBodyImpl();
        entity.setBody("  ");
        entity.setTitle("  ");

        StepVerifier.create(callback.onBeforeSave(entity, null, null))
                .expectNextMatches(e -> e.getBody().isEmpty() && e.getTitle().isEmpty())
                .verifyComplete();
    }

}
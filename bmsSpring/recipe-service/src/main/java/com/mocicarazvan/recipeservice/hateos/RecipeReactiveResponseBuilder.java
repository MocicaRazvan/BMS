package com.mocicarazvan.recipeservice.hateos;


import com.mocicarazvan.recipeservice.controllers.RecipeController;
import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;

@Component
public class RecipeReactiveResponseBuilder extends ReactiveResponseBuilder<RecipeResponse, RecipeController> {

    public RecipeReactiveResponseBuilder() {
        super(new RecipesReactiveLinkBuilder());
    }
}

package com.mocicarazvan.ingredientservice.hateos;

import com.mocicarazvan.ingredientservice.controllers.IngredientNutritionalFactController;
import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import org.springframework.stereotype.Component;

@Component
public class IngredientNutritionalValueResponseBuilder extends ReactiveResponseBuilder<IngredientNutritionalFactResponse, IngredientNutritionalFactController> {
    public IngredientNutritionalValueResponseBuilder() {
        super(new IngredientNutritionalFactReactiveLinkBuilder());
    }
}

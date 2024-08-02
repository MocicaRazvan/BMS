package com.mocicarazvan.recipeservice.services;

import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientNutritionalFactResponseWithCount;
import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientQuantityDto;
import com.mocicarazvan.recipeservice.models.IngredientQuantity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IngredientQuantityService {


    Flux<IngredientQuantity> saveAllFromIngredientList(Long recipeId, List<IngredientQuantityDto> ingredientQuantityDtos);

    Mono<Void> deleteAllByRecipeId(Long recipeId);

    Flux<IngredientNutritionalFactResponseWithCount> findAllByRecipeId(Long recipeId, String userId);

}

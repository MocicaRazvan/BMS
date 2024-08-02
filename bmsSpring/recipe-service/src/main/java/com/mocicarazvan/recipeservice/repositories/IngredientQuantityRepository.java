package com.mocicarazvan.recipeservice.repositories;

import com.mocicarazvan.recipeservice.models.IngredientQuantity;
import com.mocicarazvan.templatemodule.repositories.IdGeneratedRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IngredientQuantityRepository extends IdGeneratedRepository<IngredientQuantity> {

    Mono<Void> deleteAllByRecipeId(Long recipeId);

    Flux<IngredientQuantity> findAllByRecipeId(Long recipeId);
}

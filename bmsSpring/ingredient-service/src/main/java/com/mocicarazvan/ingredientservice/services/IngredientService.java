package com.mocicarazvan.ingredientservice.services;

import com.mocicarazvan.ingredientservice.dtos.IngredientBody;
import com.mocicarazvan.ingredientservice.dtos.IngredientResponse;
import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.ingredientservice.mappers.IngredientMapper;
import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.ingredientservice.repositories.IngredientRepository;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.services.ManyToOneUserService;
import com.mocicarazvan.templatemodule.services.ValidIds;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IngredientService extends ManyToOneUserService<
        Ingredient, IngredientBody, IngredientResponse, IngredientRepository, IngredientMapper>, ValidIds<Ingredient, IngredientRepository, IngredientResponse> {

    Flux<PageableResponse<IngredientResponse>> getAllModelsFiltered(String name, Boolean display, DietType type, PageableBody pageableBody);

    Mono<IngredientResponse> alterDisplay(Long id, Boolean display, String userId);

    Mono<Void> validIds(List<Long> ids);

    Flux<IngredientResponse> getIngredientsByIds(List<Long> ids);

    Mono<IngredientResponse> getIngredientById(Long id, String userId);

    Mono<IngredientResponse> getIngredientByIdInternal(Long id);
}

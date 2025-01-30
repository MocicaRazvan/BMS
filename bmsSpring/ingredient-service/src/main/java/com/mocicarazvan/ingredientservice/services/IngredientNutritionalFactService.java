package com.mocicarazvan.ingredientservice.services;

import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactResponse;
import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface IngredientNutritionalFactService {

    // todo add with recipie count, valid ids ca la trainings si orders vezi
    Mono<IngredientNutritionalFactResponse> deleteModel(Long id, String userId);

    Mono<IngredientNutritionalFactResponse> getModelById(Long id, String userId);

    Mono<IngredientNutritionalFactResponse> updateModel(Long id, IngredientNutritionalFactBody body, String userId);

    Flux<PageableResponse<IngredientNutritionalFactResponse>> getAllModelsFiltered(String name, Boolean display, DietType type, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, String userId, Boolean admin);

    Flux<PageableResponse<ResponseWithEntityCount<IngredientNutritionalFactResponse>>> getAllModelsFilteredWithEntityCount(String name, Boolean display, DietType type, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                                           LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, String userId, Boolean admin);


    Mono<IngredientNutritionalFactResponse> createModel(IngredientNutritionalFactBody body, String userId);

    Mono<IngredientNutritionalFactResponse> alterDisplay(Long id, Boolean display, String userId);


    Flux<IngredientNutritionalFactResponse> getModelsByIds(List<Long> ids);

    Mono<IngredientNutritionalFactResponse> getModelByIdInternal(Long id, String userId);
}

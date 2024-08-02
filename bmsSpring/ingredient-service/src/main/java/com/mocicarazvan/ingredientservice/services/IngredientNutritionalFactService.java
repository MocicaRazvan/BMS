package com.mocicarazvan.ingredientservice.services;

import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.IngredientNutritionalFactResponse;
import com.mocicarazvan.ingredientservice.dtos.IngredientResponse;
import com.mocicarazvan.ingredientservice.enums.DietType;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IngredientNutritionalFactService {

    // todo add with recipie count, valid ids ca la trainings si orders vezi
    Mono<IngredientNutritionalFactResponse> deleteModel(Long id, String userId);

    Mono<IngredientNutritionalFactResponse> getModelById(Long id, String userId);

    Mono<IngredientNutritionalFactResponse> updateModel(Long id, IngredientNutritionalFactBody body, String userId);

    Flux<PageableResponse<IngredientNutritionalFactResponse>> getAllModelsFiltered(String name, Boolean display, DietType type, PageableBody pageableBody, String userId);

    Flux<PageableResponse<ResponseWithEntityCount<IngredientNutritionalFactResponse>>> getAllModelsFilteredWithEntityCount(String name, Boolean display, DietType type, PageableBody pageableBody, String userId);


    Mono<IngredientNutritionalFactResponse> createModel(IngredientNutritionalFactBody body, String userId);

    Mono<IngredientNutritionalFactResponse> alterDisplay(Long id, Boolean display, String userId);


    Flux<IngredientNutritionalFactResponse> getModelsByIds(List<Long> ids);

    Mono<IngredientNutritionalFactResponse> getModelByIdInternal(Long id, String userId);
}

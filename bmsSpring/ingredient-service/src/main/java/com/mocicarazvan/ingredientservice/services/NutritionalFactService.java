package com.mocicarazvan.ingredientservice.services;

import com.mocicarazvan.ingredientservice.dtos.NutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactResponse;
import com.mocicarazvan.ingredientservice.mappers.NutritionalFactMapper;
import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import com.mocicarazvan.ingredientservice.repositories.NutritionalFactRepository;
import com.mocicarazvan.templatemodule.services.ManyToOneUserService;
import reactor.core.publisher.Mono;

public interface NutritionalFactService extends ManyToOneUserService<
        NutritionalFact, NutritionalFactBody, NutritionalFactResponse, NutritionalFactRepository, NutritionalFactMapper> {

    Mono<NutritionalFactResponse> findByIngredientIdUserId(Long ingredientId, String userId);


    Mono<NutritionalFactResponse> findByIngredientId(Long ingredientId);

    Mono<NutritionalFactResponse> createModel(NutritionalFactBody modelBody, Long referenceId, String userId);
}

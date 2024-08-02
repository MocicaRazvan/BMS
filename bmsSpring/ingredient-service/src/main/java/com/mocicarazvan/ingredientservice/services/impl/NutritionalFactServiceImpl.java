package com.mocicarazvan.ingredientservice.services.impl;

import com.mocicarazvan.ingredientservice.dtos.NutritionalFactBody;
import com.mocicarazvan.ingredientservice.dtos.NutritionalFactResponse;
import com.mocicarazvan.ingredientservice.mappers.NutritionalFactMapper;
import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import com.mocicarazvan.ingredientservice.repositories.NutritionalFactRepository;
import com.mocicarazvan.ingredientservice.services.IngredientService;
import com.mocicarazvan.ingredientservice.services.NutritionalFactService;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class NutritionalFactServiceImpl extends
        ManyToOneUserServiceImpl<
                NutritionalFact, NutritionalFactBody, NutritionalFactResponse, NutritionalFactRepository, NutritionalFactMapper>
        implements NutritionalFactService {
    public NutritionalFactServiceImpl(NutritionalFactRepository modelRepository, NutritionalFactMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, IngredientService ingredientService) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "nutritionalFact", List.of("createdAt", "updatedAt", "id", "fat", "saturated_fat",
                "carbohydrates", "sugar", "protein", "salt", "unit", "ingredient_id"
        ));
        this.ingredientService = ingredientService;
    }

    private final IngredientService ingredientService;

    @Override
    public Mono<NutritionalFactResponse> findByIngredientIdUserId(Long ingredientId, String userId) {
        return ingredientService.getModelById(ingredientId, userId)
                .flatMap(i -> modelRepository.findByIngredientId(i.getId()))
                .map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<NutritionalFactResponse> findByIngredientId(Long ingredientId) {
        return modelRepository.findByIngredientId(ingredientId)
                .map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<NutritionalFactResponse> createModel(NutritionalFactBody modelBody, Long referenceId, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> {
                    NutritionalFact model = modelMapper.fromBodyToModel(modelBody);
                    model.setUserId(authUser.getId());
                    model.setIngredientId(referenceId);
                    return modelRepository.save(model)
                            .map(modelMapper::fromModelToResponse);
                });
    }
}

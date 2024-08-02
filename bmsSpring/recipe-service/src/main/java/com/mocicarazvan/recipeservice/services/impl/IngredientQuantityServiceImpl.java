package com.mocicarazvan.recipeservice.services.impl;

import com.mocicarazvan.recipeservice.clients.IngredientClient;
import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientNutritionalFactResponseWithCount;
import com.mocicarazvan.recipeservice.dtos.ingredients.IngredientQuantityDto;
import com.mocicarazvan.recipeservice.mappers.IngredientQuantityMapper;
import com.mocicarazvan.recipeservice.models.IngredientQuantity;
import com.mocicarazvan.recipeservice.repositories.IngredientQuantityRepository;
import com.mocicarazvan.recipeservice.services.IngredientQuantityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientQuantityServiceImpl implements IngredientQuantityService {
    private final IngredientQuantityRepository ingredientQuantityRepository;
    private final IngredientQuantityMapper ingredientQuantityMapper;
    private final IngredientClient ingredientClient;

    @Override
    public Flux<IngredientQuantity> saveAllFromIngredientList(Long recipeId, List<IngredientQuantityDto> ingredientQuantityDtos) {
        List<IngredientQuantity> ings = ingredientQuantityDtos.stream().map(
                ing -> {
                    IngredientQuantity ingredientQuantity = ingredientQuantityMapper.fromDtoToModel(ing);
                    ingredientQuantity.setRecipeId(recipeId);
                    return ingredientQuantity;
                }
        ).toList();
        return ingredientQuantityRepository.saveAll(ings);
    }

    @Override
    public Mono<Void> deleteAllByRecipeId(Long recipeId) {
        return ingredientQuantityRepository.deleteAllByRecipeId(recipeId);
    }

    @Override
    public Flux<IngredientNutritionalFactResponseWithCount> findAllByRecipeId(Long recipeId, String userId) {
        return ingredientQuantityRepository.findAllByRecipeId(recipeId).flatMap(
                ing -> ingredientClient.getByIdWithInfo(String.valueOf(ing.getIngredientId()), userId)
                        .map(ingInfo -> new IngredientNutritionalFactResponseWithCount(ingInfo, ing.getQuantity()))
        );
    }


}

package com.mocicarazvan.dayservice.services;

import com.mocicarazvan.dayservice.dtos.meal.MealBody;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.dayservice.enums.DietType;
import com.mocicarazvan.dayservice.mappers.MealMapper;
import com.mocicarazvan.dayservice.models.Meal;
import com.mocicarazvan.dayservice.repositories.MealRepository;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithChildList;
import com.mocicarazvan.templatemodule.services.ManyToOneUserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MealService extends ManyToOneUserService<
        Meal, MealBody, MealResponse, MealRepository, MealMapper
        > {

    Flux<RecipeResponse> getRecipesByMeal(Long id, String userId);

    Flux<RecipeResponse> getRecipesByMealInternal(Long id, String userId);

    Flux<MealResponse> getMealsByDay(Long dayId, String userId);

    Flux<MealResponse> getMealsByDayInternal(Long dayId, String userId);

    Mono<Void> deleteAllByDay(Long dayId);

    Flux<ResponseWithChildList<MealResponse, RecipeResponse>> getMealsByDayWithRecipes(Long dayId, String userId);

    Mono<Boolean> existsByDayIdAndRecipeId(Long dayId, Long recipeId);

    Mono<DietType> determineMostRestrictiveDietTypeByDay(List<Long> dayIds, String userId);

    Mono<MealResponse> createModelCustomVerify(MealBody mealBody, List<Long> idsToNotVerify, String userId);
}

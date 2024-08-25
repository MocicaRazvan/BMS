package com.mocicarazvan.dayservice.services.impl;

import com.mocicarazvan.dayservice.clients.PlanClient;
import com.mocicarazvan.dayservice.clients.RecipeClient;
import com.mocicarazvan.dayservice.dtos.meal.MealBody;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.dayservice.enums.DietType;
import com.mocicarazvan.dayservice.mappers.MealMapper;
import com.mocicarazvan.dayservice.models.Meal;
import com.mocicarazvan.dayservice.repositories.MealRepository;
import com.mocicarazvan.dayservice.services.MealService;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithChildList;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service

public class MealServiceImpl
        extends ManyToOneUserServiceImpl<Meal, MealBody, MealResponse, MealRepository, MealMapper>
        implements MealService {

    private final RecipeClient recipeClient;
    private final PlanClient planClient;
    private final EntitiesUtils entitiesUtils;

    public MealServiceImpl(MealRepository modelRepository, MealMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, RecipeClient recipeClient, PlanClient planClient, EntitiesUtils entitiesUtils) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "meal", List.of("id", "userId", "period", "title", "createdAt", "updatedAt"));
        this.recipeClient = recipeClient;
        this.planClient = planClient;
        this.entitiesUtils = entitiesUtils;
    }


    @Override
    public Flux<RecipeResponse> getRecipesByMeal(Long id, String userId) {
        return getModelById(id, userId)
                .flatMapMany(model -> recipeClient.getByIds(model.getRecipes().stream().map(Object::toString).toList(), userId));
    }

    @Override
    public Flux<RecipeResponse> getRecipesByMealInternal(Long id, String userId) {
        return getModel(id)
                .flatMapMany(model -> recipeClient.getByIds(model.getRecipes().stream().map(Object::toString).toList(), userId));
    }

    @Override
    public Flux<MealResponse> getMealsByDay(Long dayId, String userId) {

        return
                userClient.getUser("", userId)
                        .flatMapMany(user ->
                                modelRepository.findAllByDayId(dayId, Sort.by(Sort.Order.asc("period")))
                                        .flatMap(model -> entitiesUtils.checkEntityOwnerOrAdmin(model, user)
                                                .thenReturn(modelMapper.fromModelToResponse(model))));

    }

    @Override
    public Flux<MealResponse> getMealsByDayInternal(Long dayId, String userId) {
        return modelRepository.findAllByDayId(dayId, Sort.by(Sort.Order.asc("period")))
                .map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<Void> deleteAllByDay(Long dayId) {
        return modelRepository.deleteAllByDayId(dayId);
    }

    @Override
    public Flux<ResponseWithChildList<MealResponse, RecipeResponse>> getMealsByDayWithRecipes(Long dayId, String userId) {
        return getMealsByDay(dayId, userId)
                .flatMap(mr -> recipeClient.getByIds(mr.getRecipes().stream().map(Object::toString).toList(), userId)
                        .collectList()
                        .map(recipes -> new ResponseWithChildList<>(mr, recipes)));
    }

    @Override
    public Mono<Boolean> existsByDayIdAndRecipeId(Long dayId, Long recipeId) {
        return modelRepository.existsByDayIdAndRecipesContaining(dayId, recipeId);
    }

    @Override
    public Mono<DietType> determineMostRestrictiveDietTypeByDay(List<Long> dayIds, String userId) {
        return modelRepository.findUniqueRecipeIdsByDayIds(dayIds.toArray(new Long[0]))
                .collectList()
                .flatMap(ids -> recipeClient.determineMostRestrictiveDietType(ids.stream().map(Object::toString).toList(), userId));
    }

    @Override
    public Mono<MealResponse> createModel(MealBody mealBody, String userId) {
        return recipeClient.verifyIds(mealBody.getRecipes().stream().map(Object::toString).toList(), userId)
                .then(super.createModel(mealBody, userId));
    }

    @Override
    public Mono<MealResponse> updateModel(Long id, MealBody mealBody, String userId) {
        return getModelById(id, userId)
                .map(meal -> mealBody.getRecipes().stream().filter(r -> !meal.getRecipes().contains(r))
                        .map(Object::toString).toList())
                .flatMap(ids -> recipeClient.verifyIds(ids, userId))
                .then(super.updateModel(id, mealBody, userId));

    }

    @Override
    public Mono<MealResponse> deleteModel(Long id, String userId) {
        return planClient.getCountInParent(id, userId)
                .flatMap(count -> {
                    if (count.getCount() > 0) {
                        return Mono.error(new SubEntityUsed("plan", id));
                    }
                    return super.deleteModel(id, userId);
                });
    }
}

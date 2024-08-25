package com.mocicarazvan.dayservice.services.impl;

import com.mocicarazvan.dayservice.clients.PlanClient;
import com.mocicarazvan.dayservice.clients.RecipeClient;
import com.mocicarazvan.dayservice.dtos.day.DayBody;
import com.mocicarazvan.dayservice.dtos.day.DayBodyWithMeals;
import com.mocicarazvan.dayservice.dtos.day.DayResponse;
import com.mocicarazvan.dayservice.dtos.meal.MealBody;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.dayservice.mappers.DayMapper;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.dayservice.repositories.DayRepository;
import com.mocicarazvan.dayservice.repositories.ExtendedDayRepository;
import com.mocicarazvan.dayservice.services.DayService;
import com.mocicarazvan.dayservice.services.MealService;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.services.impl.TitleBodyServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DayServiceImpl
        extends TitleBodyServiceImpl<Day, DayBody, DayResponse, DayRepository, DayMapper>
        implements DayService {

    private final MealService mealService;
    private final TransactionalOperator transactionalOperator;
    private final ExtendedDayRepository extendedDayRepository;
    private final PlanClient planClient;
    private final RecipeClient recipeClient;


    public DayServiceImpl(DayRepository modelRepository, DayMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, MealService mealService, TransactionalOperator transactionalOperator, ExtendedDayRepository extendedDayRepository, PlanClient planClient, RecipeClient recipeClient) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "day", List.of("id", "userId", "type", "title", "createdAt", "updatedAt"), entitiesUtils);
        this.mealService = mealService;
        this.transactionalOperator = transactionalOperator;
        this.extendedDayRepository = extendedDayRepository;
        this.planClient = planClient;
        this.recipeClient = recipeClient;
    }

    // todo admin route
    @Override
    public Flux<PageableResponse<DayResponse>> getDaysFiltered(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId) {
        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr -> pageableUtils.createPageableResponse(
                        extendedDayRepository.getDaysFiltered(title, type, pr, excludeIds).map(modelMapper::fromModelToResponse),
                        extendedDayRepository.countDayFiltered(title, type, excludeIds),
                        pr
                ));
    }

    // todo admin route
    @Override
    public Flux<PageableResponse<ResponseWithUserDto<DayResponse>>> getDaysFilteredWithUser(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId) {
        return getDaysFiltered(title, type, excludeIds, pageableBody, userId)
                .concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<DayResponse>>> getDaysFilteredWithCount(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId) {
        return getDaysFiltered(title, type, excludeIds, pageableBody, userId)
                .concatMap(pr -> toResponseWithCount(userId, planClient, pr));
    }

    @Override
    public Flux<PageableResponse<DayResponse>> getDaysFilteredTrainer(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Long trainerId) {
        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr -> pageableUtils.createPageableResponse(
                        extendedDayRepository.getDaysFilteredTrainer(title, type, pr, excludeIds, trainerId).map(modelMapper::fromModelToResponse),
                        extendedDayRepository.countDayFilteredTrainer(title, type, excludeIds, trainerId),
                        pr
                ));
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<DayResponse>>> getDaysFilteredTrainerWithCount(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Long trainerId) {
        return getDaysFilteredTrainer(title, type, excludeIds, pageableBody, userId, trainerId)
                .concatMap(pr -> toResponseWithCount(userId, planClient, pr));
    }

    @Override
    public Mono<Void> validIds(List<Long> ids) {
        return this.validIds(ids, modelRepository, modelName);
    }

    @Override
    public Mono<DayResponse> createWithMeals(DayBodyWithMeals dayBodyWithMeals, String userId) {
//        return super.createModel(dayBodyWithMeals, userId)
//                .flatMap(day -> Flux.fromIterable(dayBodyWithMeals.getMeals())
//                        .flatMap(body -> mealService.createModel(MealBody.fromCompose(body, day.getId()), userId))
//                        .then(Mono.just(day))
//                        .onErrorResume(e -> deleteModel(day.getId(), userId).then(Mono.error(e)))
//                );
        return transactionalOperator.transactional(
                super.createModel(dayBodyWithMeals, userId)
                        .flatMap(day ->
                                Flux.fromIterable(dayBodyWithMeals.getMeals())
                                        .flatMap(body -> mealService.createModel(MealBody.fromCompose(body, day.getId()), userId))
                                        .then(Mono.just(day))
                        )
                        .onErrorMap(e -> {
                            log.error("Error creating day with meals", e);
                            return new IllegalActionException("Recipe ids are invalid at creating");
                        })
        );
    }

    @Override
    public Mono<DayResponse> updateWithMeals(Long id, DayBodyWithMeals dayBodyWithMeals, String userId) {

//        return getModelById(id, userId)
//                .flatMap(existingDay ->
//                        super.updateModel(id, dayBodyWithMeals, userId)
//                                .flatMap(updatedDay -> Flux.fromIterable(dayBodyWithMeals.getMeals())
//                                        .flatMap(body -> mealService.createModel(MealBody.fromCompose(body, updatedDay.getId()), userId))
//                                        .then(Mono.just(updatedDay))
//                                )
//                                .onErrorResume(e -> super.updateModel(id, DayBody.builder()
//                                        .type(existingDay.getType())
//                                        .title(existingDay.getTitle())
//                                        .body(existingDay.getBody())
//                                        .build(), userId).then(Mono.error(e)))
//                );

        return transactionalOperator.transactional(
                        updateModelWithSuccess(id, userId, day -> mealService.deleteAllByDay(day.getId())
                                .then(modelMapper.updateModelFromBody(dayBodyWithMeals, day)))
                                .flatMap(updatedDay ->
                                        Flux.fromIterable(dayBodyWithMeals.getMeals())
                                                .flatMap(body -> mealService.createModel(MealBody.fromCompose(body, updatedDay.getId()), userId))
                                                .then(Mono.just(updatedDay))
                                )
                )
                .onErrorMap(e -> {
                            log.error("Error updating day with meals", e);
                            return new IllegalActionException("Recipe ids are invalid at updating");
                        }
                );
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUserInternal(Long id, Long recipeId, String userId) {
//        return getModel(id)
//                .thenMany(mealService.getMealsByDayInternal(id, userId))
//                .map(MealResponse::getRecipes)
//                .collectList()
//                .flatMap(recipes -> {
//                    if (recipes.stream().flatMap(List::stream).noneMatch(r -> r.equals(recipeId))) {
//                        return Mono.error(new IllegalActionException("Recipe not found in day"));
//                    }
//                    return recipeClient.getByIdWithUser(String.valueOf(recipeId), userId);
//                });

        return getModel(id)
                .then(mealService.existsByDayIdAndRecipeId(id, recipeId))
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalActionException("Recipe not found in day"));
                    }
                    return recipeClient.getByIdWithUser(String.valueOf(recipeId), userId);
                });
    }

    @Override
    public Mono<ResponseWithUserDto<DayResponse>> getModelByIdWithUserInternal(Long id, String userId) {
        return getModel(id)
                .flatMap(model -> userClient.getUser("", model.getUserId().toString())
                        .map(user -> ResponseWithUserDto.<DayResponse>builder()
                                .model(modelMapper.fromModelToResponse(model))
                                .user(user)
                                .build())
                );
    }

    @Override
    public Mono<EntityCount> countInParent(Long childId) {
        return modelRepository.countInParent(childId)
                .map(EntityCount::new);
    }

    @Override
    public Flux<DayResponse> getModelsByIds(List<Long> ids) {
        return modelRepository.findAllByIdIn(ids)
                .collectMap(Day::getId, modelMapper::fromModelToResponse)
                .flatMapMany(map -> Flux.fromIterable(ids)
                        .map(map::get)
                        .filter(Objects::nonNull)
                );
    }
}

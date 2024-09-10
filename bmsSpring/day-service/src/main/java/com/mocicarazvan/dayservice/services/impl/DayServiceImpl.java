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
import com.mocicarazvan.templatemodule.adapters.CacheBaseFilteredToHandlerAdapter;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCache;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.services.impl.TitleBodyServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function7;
import org.springframework.stereotype.Component;
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
    private final DayServiceCacheHandler dayServiceCacheHandler;


    public DayServiceImpl(DayRepository modelRepository, DayMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, MealService mealService, TransactionalOperator transactionalOperator, ExtendedDayRepository extendedDayRepository, PlanClient planClient, RecipeClient recipeClient, DayServiceCacheHandler dayServiceCacheHandler) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "day", List.of("id", "userId", "type", "title", "createdAt", "updatedAt"), entitiesUtils, dayServiceCacheHandler);
        this.mealService = mealService;
        this.transactionalOperator = transactionalOperator;
        this.extendedDayRepository = extendedDayRepository;
        this.planClient = planClient;
        this.recipeClient = recipeClient;
        this.dayServiceCacheHandler = dayServiceCacheHandler;
    }

    // todo admin route
    @Override
    public Flux<PageableResponse<DayResponse>> getDaysFiltered(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Boolean admin) {
        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr ->
                        dayServiceCacheHandler.getDaysFilteredPersist.apply(
                                pageableUtils.createPageableResponse(
                                        extendedDayRepository.getDaysFiltered(title, type, pr, excludeIds).map(modelMapper::fromModelToResponse),
                                        extendedDayRepository.countDayFiltered(title, type, excludeIds),
                                        pr
                                ), title, type, excludeIds, pageableBody, userId, admin));
    }

    // todo admin route
    @Override
    public Flux<PageableResponse<ResponseWithUserDto<DayResponse>>> getDaysFilteredWithUser(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Boolean admin) {
        return getDaysFiltered(title, type, excludeIds, pageableBody, userId, admin)
                .concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<DayResponse>>> getDaysFilteredWithCount(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Boolean admin) {
        return getDaysFiltered(title, type, excludeIds, pageableBody, userId, admin)
                .concatMap(pr -> toResponseWithCount(userId, planClient, pr));
    }

    @Override
    public Flux<PageableResponse<DayResponse>> getDaysFilteredTrainer(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Long trainerId) {
        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr ->
                        dayServiceCacheHandler.getDaysFilteredTrainerPersist.apply(
                                pageableUtils.createPageableResponse(
                                        extendedDayRepository.getDaysFilteredTrainer(title, type, pr, excludeIds, trainerId).map(modelMapper::fromModelToResponse),
                                        extendedDayRepository.countDayFilteredTrainer(title, type, excludeIds, trainerId),
                                        pr
                                ), title, type, excludeIds, pageableBody, userId, trainerId));
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<DayResponse>>> getDaysFilteredTrainerWithCount(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Long trainerId) {
        return getDaysFilteredTrainer(title, type, excludeIds, pageableBody, userId, trainerId)
                .concatMap(pr -> toResponseWithCount(userId, planClient, pr));
    }

    @Override
    public Mono<Void> validIds(List<Long> ids) {
        return
                dayServiceCacheHandler.validIdsPersist.apply(
                                this.validIds(ids, modelRepository, modelName)
                                        .thenReturn(true), ids)
                        .then();
    }

    @Override
    public Mono<DayResponse> createWithMeals(DayBodyWithMeals dayBodyWithMeals, String userId) {
//        return super.createModel(dayBodyWithMeals, userId)
//                .flatMap(day -> Flux.fromIterable(dayBodyWithMeals.getMeals())
//                        .flatMap(body -> mealService.createModel(MealBody.fromCompose(body, day.getId()), userId))
//                        .then(Mono.just(day))
//                        .onErrorResume(e -> deleteModel(day.getId(), userId).then(Mono.error(e)))
//                );
        return

                dayServiceCacheHandler.getCreateModelInvalidate().apply(
                        transactionalOperator.transactional(
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
                        ), dayBodyWithMeals, userId);
    }

    @Override
    public Mono<DayResponse> updateWithMeals(Long id, DayBodyWithMeals dayBodyWithMeals, String userId) {


        return

                dayServiceCacheHandler.getUpdateModelInvalidate().apply(
                        transactionalOperator.transactional(
                                        mealService.getMealsByDay(id, userId)
                                                .map(MealResponse::getRecipes)
                                                .flatMap(Flux::fromIterable)
                                                .collectList()
                                                .flatMap(recipeIds ->
                                                        updateModelWithSuccess(id, userId, day -> mealService.deleteAllByDay(day.getId())
                                                                .then(modelMapper.updateModelFromBody(dayBodyWithMeals, day)))
                                                                .flatMap(updatedDay ->
                                                                        Flux.fromIterable(dayBodyWithMeals.getMeals())
                                                                                .flatMap(body -> mealService.createModelCustomVerify(MealBody.fromCompose(body, updatedDay.getId()), recipeIds, userId))
                                                                                .then(Mono.just(updatedDay))
                                                                )
                                                ))
                                .onErrorMap(e -> {
                                            log.error("Error updating day with meals", e);
                                            return new IllegalActionException("Recipe ids are invalid at updating");
                                        }
                                ), id, dayBodyWithMeals, userId);
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUserInternal(Long id, Long recipeId, String userId) {

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
        return
                modelRepository.countInParent(childId)
                        .collectList()
                        .map(EntityCount::new);
    }

    @Override
    public Flux<DayResponse> getModelsByIds(List<Long> ids) {
        return
                dayServiceCacheHandler.getModelsByIdsPersist.apply(
                        modelRepository.findAllByIdIn(ids)
                                .collectMap(Day::getId, modelMapper::fromModelToResponse)
                                .flatMapMany(map -> Flux.fromIterable(ids)
                                        .map(map::get)
                                        .filter(Objects::nonNull)
                                ), ids);
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Component
    public static class DayServiceCacheHandler
            extends TitleBodyServiceImpl.TitleBodyServiceCacheHandler<Day, DayBody, DayResponse> {
        private final FilteredListCaffeineCache<FilterKeyType, DayResponse> cacheFilter;

        Function7<Flux<PageableResponse<DayResponse>>, String, DayType, List<Long>, PageableBody, String, Boolean, Flux<PageableResponse<DayResponse>>>
                getDaysFilteredPersist;
        Function7<Flux<PageableResponse<DayResponse>>, String, DayType, List<Long>, PageableBody, String, Long, Flux<PageableResponse<DayResponse>>> getDaysFilteredTrainerPersist;
        Function2<Mono<Boolean>, List<Long>, Mono<Boolean>> validIdsPersist;
        Function2<Flux<DayResponse>, List<Long>, Flux<DayResponse>> getModelsByIdsPersist;

        public DayServiceCacheHandler(FilteredListCaffeineCache<FilterKeyType, DayResponse> cacheFilter) {
            super();
            this.cacheFilter = cacheFilter;

            CacheBaseFilteredToHandlerAdapter.convertToTitleBodyHandler(cacheFilter, this);

            this.getDaysFilteredPersist = (flux, title, type, excludeIds, pageableBody, userId, admin) -> {
                FilterKeyType.KeyRouteType keyRouteType = Boolean.TRUE.equals(admin) ? FilterKeyType.KeyRouteType.createForAdmin() : FilterKeyType.KeyRouteType.createForPublic();
                return cacheFilter.getUniqueFluxCache(
                        EntitiesUtils.getListOfNotNullObjects(title, type, excludeIds, pageableBody, admin),
                        "getDaysFiltered",
                        m -> m.getContent().getId(),
                        keyRouteType,
                        flux
                );
            };


            this.getDaysFilteredTrainerPersist = (flux, title, type, excludeIds, pageableBody, userId, trainerId) -> cacheFilter.getUniqueFluxCacheForTrainer(
                    EntitiesUtils.getListOfNotNullObjects(title, type, excludeIds, pageableBody, trainerId),
                    trainerId,
                    "getDaysFilteredTrainer",
                    m -> m.getContent().getId(),
                    flux
            );

            this.validIdsPersist = (mono, ids) -> cacheFilter.getUniqueMonoCacheIdListIndependent(
                    EntitiesUtils.getListOfNotNullObjects(ids),
                    "validIdsPersist" + ids,
                    ids,
                    mono
            );


            this.getModelsByIdsPersist = (flux, ids) -> cacheFilter.getUniqueFluxCacheIndependent(
                    EntitiesUtils.getListOfNotNullObjects(ids),
                    "getModelsByIdsPersist" + ids,
                    IdGenerateDto::getId,
                    flux
            );
        }
    }
}

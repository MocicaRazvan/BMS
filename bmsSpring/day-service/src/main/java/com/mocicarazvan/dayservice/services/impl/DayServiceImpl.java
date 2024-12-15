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
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCacheEvict;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.services.ValidIds;
import com.mocicarazvan.templatemodule.services.impl.TitleBodyServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
        extends TitleBodyServiceImpl<Day, DayBody, DayResponse, DayRepository, DayMapper, DayServiceImpl.DayServiceRedisCacheWrapper>
        implements DayService {

    private final MealService mealService;
    private final TransactionalOperator transactionalOperator;
    private final ExtendedDayRepository extendedDayRepository;
    private final PlanClient planClient;
    private final RecipeClient recipeClient;
    private final DayEmbedServiceImpl dayEmbedServiceImpl;

    public DayServiceImpl(DayRepository modelRepository, DayMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, MealService mealService, TransactionalOperator transactionalOperator, ExtendedDayRepository extendedDayRepository, PlanClient planClient, RecipeClient recipeClient, DayServiceRedisCacheWrapper self, DayEmbedServiceImpl dayEmbedServiceImpl) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "day", List.of("id", "userId", "type", "title", "createdAt", "updatedAt"), entitiesUtils, self);
        this.mealService = mealService;
        this.transactionalOperator = transactionalOperator;
        this.extendedDayRepository = extendedDayRepository;
        this.planClient = planClient;
        this.recipeClient = recipeClient;

        this.dayEmbedServiceImpl = dayEmbedServiceImpl;
    }

    @Override
    public Mono<List<String>> seedEmbeddings() {
        return modelRepository.findAll()
                .flatMap(day ->
                        dayEmbedServiceImpl.saveEmbedding(day.getId(), day.getTitle()).then(Mono.just("Seeded embeddings for day: " + day.getId())))
                .collectList()
                .as(transactionalOperator::transactional);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id", masterId = "#userId")
    public Mono<DayResponse> deleteModel(Long id, String userId) {
        return super.deleteModel(id, userId);

    }

    @Override
    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
    public Flux<PageableResponse<DayResponse>> getAllModels(PageableBody pageableBody, String userId) {
        return super.getAllModels(pageableBody, userId);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, id = "#id", masterId = "#userId")
    public Mono<DayResponse> updateModel(Long id, DayBody body, String userId) {
        return updateModelWithSuccess(id, userId, model ->
                dayEmbedServiceImpl.updateEmbeddingWithZip(body.getTitle(), model.getTitle(), model.getId(), modelMapper.updateModelFromBody(body, model))).as(transactionalOperator::transactional);
    }


    // todo admin route
    @Override
    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id", masterId = "#userId")
    public Flux<PageableResponse<DayResponse>> getDaysFiltered(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Boolean admin) {

        return self.getDaysFiltered(title, type, excludeIds, pageableBody, userId, admin, allowedSortingFields);
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
    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id", masterId = "#trainerId")
    public Flux<PageableResponse<DayResponse>> getDaysFilteredTrainer(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Long trainerId) {

        return self.getDaysFilteredTrainer(title, type, excludeIds, pageableBody, userId, trainerId, allowedSortingFields);
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<DayResponse>>> getDaysFilteredTrainerWithCount(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Long trainerId) {
        return getDaysFilteredTrainer(title, type, excludeIds, pageableBody, userId, trainerId)
                .concatMap(pr -> toResponseWithCount(userId, planClient, pr));
    }

    @Override
    public Mono<Void> validIds(List<Long> ids) {

        return
                this.validIds(ids, modelRepository, modelName)
                        .then();

    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterId = "#userId")
    public Mono<DayResponse> createWithMeals(DayBodyWithMeals dayBodyWithMeals, String userId) {

        return

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
                                }).flatMap(d -> dayEmbedServiceImpl.saveEmbedding(d.getId(), d.getTitle())
                                        .thenReturn(d)
                                )
                );
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterId = "#userId", id = "#id")
    public Mono<DayResponse> updateWithMeals(Long id, DayBodyWithMeals dayBodyWithMeals, String userId) {

        return

                transactionalOperator.transactional(
                                mealService.getMealsByDay(id, userId)
                                        .map(MealResponse::getRecipes)
                                        .flatMap(Flux::fromIterable)
                                        .collectList()
                                        .flatMap(recipeIds ->
                                                updateModelWithSuccess(id, userId, day ->
                                                        dayEmbedServiceImpl.updateEmbeddingWithZip(dayBodyWithMeals.getTitle(),
                                                                        day.getTitle(), day.getId(),
                                                                        mealService.deleteAllByDay(day.getId()))
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
                        );
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUserInternal(Long id, Long recipeId, String userId) {

        return self.getModel(id)
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
        return self.getModelInternal(id)
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
    @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
    public Flux<DayResponse> getModelsByIds(List<Long> ids) {

        return
                modelRepository.findAllByIdIn(ids)
                        .collectMap(Day::getId, modelMapper::fromModelToResponse)
                        .flatMapMany(map -> Flux.fromIterable(ids)
                                .map(map::get)
                                .filter(Objects::nonNull)
                        );
    }

    @Getter
    @Component
    public static class DayServiceRedisCacheWrapper extends TitleBodyServiceImpl.TitleBodyServiceRedisCacheWrapper<Day, DayBody, DayResponse, DayRepository, DayMapper>
            implements ValidIds<Day, DayRepository, DayResponse> {

        private final DayRepository dayRepository;
        private final DayMapper dayMapper;
        private final PageableUtilsCustom pageableUtils;
        private final ExtendedDayRepository extendedDayRepository;
        private final MealService mealService;

        public DayServiceRedisCacheWrapper(DayRepository modelRepository, DayMapper modelMapper, DayRepository dayRepository, DayMapper dayMapper, PageableUtilsCustom pageableUtils, ExtendedDayRepository extendedDayRepository, UserClient userClient, MealService mealService) {
            super(modelRepository, modelMapper, "day", userClient);
            this.dayRepository = dayRepository;
            this.dayMapper = dayMapper;
            this.pageableUtils = pageableUtils;
            this.extendedDayRepository = extendedDayRepository;
            this.mealService = mealService;
        }

        @Override
        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "entity.id")
        public Flux<MonthlyEntityGroup<DayResponse>> getModelGroupedByMonthBase(int month, UserDto userDto) {
            return super.getModelGroupedByMonthBase(month, userDto);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<Day> findAllById(List<Long> ids) {
            return super.findAllById(ids);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Day> getModel(Long id) {
            return super.getModel(id);
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Day> getModelInternal(Long id) {
            return super.getModel(id);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserDto<DayResponse>> getModelByIdWithUserBase(UserDto authUser, Long id) {
            return super.getModelByIdWithUserBase(authUser, id);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserLikesAndDislikes<DayResponse>> getModelByIdWithUserLikesAndDislikesBase(Long id, UserDto authUser) {
            return super.getModelByIdWithUserLikesAndDislikesBase(id, authUser);
        }


        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id", masterId = "#userId")
        public Flux<PageableResponse<DayResponse>> getDaysFiltered(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Boolean admin, List<String> allowedSortingFields) {
            return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                    .then(pageableUtils.createPageRequest(pageableBody))
                    .flatMapMany(pr ->
                            pageableUtils.createPageableResponse(
                                    extendedDayRepository.getDaysFiltered(title, type, pr, excludeIds).map(modelMapper::fromModelToResponse),
                                    extendedDayRepository.countDayFiltered(title, type, excludeIds),
                                    pr
                            ));
        }

        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id", masterId = "#trainerId")
        public Flux<PageableResponse<DayResponse>> getDaysFilteredTrainer(String title, DayType type, List<Long> excludeIds, PageableBody pageableBody, String userId, Long trainerId, List<String> allowedSortingFields) {
            return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                    .then(pageableUtils.createPageRequest(pageableBody))
                    .flatMapMany(pr ->
                            pageableUtils.createPageableResponse(
                                    extendedDayRepository.getDaysFilteredTrainer(title, type, pr, excludeIds, trainerId).map(modelMapper::fromModelToResponse),
                                    extendedDayRepository.countDayFilteredTrainer(title, type, excludeIds, trainerId),
                                    pr
                            ));
        }


        @Override
        public Flux<DayResponse> getModelsByIds(List<Long> ids) {
            return null;
        }


        public Mono<Boolean> getModelRecipeInternal(Long id, Long recipeId) {
            return getModel(id)
                    .then(mealService.existsByDayIdAndRecipeId(id, recipeId));
        }

        @Override
        @RedisReactiveCacheEvict(key = CACHE_KEY_PATH, id = "#r.id")
        public Mono<DayResponse> reactToModelInvalidate(DayResponse r) {
            return super.reactToModelInvalidate(r);
        }
    }


}

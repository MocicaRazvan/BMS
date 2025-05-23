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
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithChildList;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.repositories.AssociativeEntityRepository;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class MealServiceImpl
        extends ManyToOneUserServiceImpl<Meal, MealBody, MealResponse, MealRepository, MealMapper,
        MealServiceImpl.MealServiceRedisCacheWrapper
        >
        implements MealService {

    private final RecipeClient recipeClient;
    private final PlanClient planClient;
    private final EntitiesUtils entitiesUtils;
    private final MealRepository mealRepository;
    private final AssociativeEntityRepository associativeEntityRepository;
    private final TransactionalOperator transactionalOperator;


    public MealServiceImpl(MealRepository modelRepository, MealMapper modelMapper,
                           PageableUtilsCustom pageableUtils, UserClient userClient, RecipeClient recipeClient,
                           PlanClient planClient, EntitiesUtils entitiesUtils, MealRepository mealRepository,
                           MealServiceRedisCacheWrapper self, RabbitMqUpdateDeleteService<Meal> rabbitMqUpdateDeleteService,
                           @Qualifier("mealRecipesRepository") AssociativeEntityRepository associativeEntityRepository, TransactionalOperator transactionalOperator) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "meal",
                List.of("id", "userId", "period", "title", "createdAt", "updatedAt"), self, rabbitMqUpdateDeleteService);
        this.recipeClient = recipeClient;
        this.planClient = planClient;
        this.entitiesUtils = entitiesUtils;
        this.mealRepository = mealRepository;
        this.associativeEntityRepository = associativeEntityRepository;
        this.transactionalOperator = transactionalOperator;
    }


    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
    public Flux<PageableResponse<MealResponse>> getAllModels(PageableBody pageableBody, String userId) {
        return super.getAllModels(pageableBody, userId);
    }


    @Override
    @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "content.id")
    public Flux<PageableResponse<MealResponse>> getModelsByIdInPageable(List<Long> ids, PageableBody pageableBody) {
        return super.getModelsByIdInPageable(ids, pageableBody);
    }

    @Override
    public Flux<RecipeResponse> getRecipesByMeal(Long id, String userId) {
        return getModelById(id, userId)
                .flatMapMany(model -> recipeClient.getByIds(model.getRecipes().stream().map(Object::toString).toList(), userId));
    }

    @Override
    public Flux<RecipeResponse> getRecipesByMealInternal(Long id, String userId) {
        return self.getModelInternal(id)
                .flatMapMany(model -> recipeClient.getByIds(model.getRecipes().stream().map(Object::toString).toList(), userId));
    }

    @Override
    public Flux<MealResponse> getMealsByDay(Long dayId, String userId) {

        return
                userClient.getUser("", userId)
                        .flatMapMany(user ->
                                mealRepository.findAllByDayIdCustomPeriodSort(dayId)
                                        .flatMap(model -> entitiesUtils.checkEntityOwnerOrAdmin(model, user)
                                                .thenReturn(modelMapper.fromModelToResponse(model))));

    }


    @Override
    public Flux<MealResponse> getMealsByDayInternal(Long dayId, String userId) {

        return

                self.findAllByDayIdCustomPeriodSort(dayId)
                        .map(modelMapper::fromModelToResponse);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterId = "#dayId")
    public Mono<Void> deleteAllByDay(Long dayId) {

        return
                mealRepository.findAllByDayId(dayId)
                        .collectList()
                        .doOnSuccess(rabbitMqUpdateDeleteService::sendBatchDeleteMessage)
                        .then(
                                modelRepository.deleteAllByDayId(dayId));
    }

    @Override
    public Flux<ResponseWithChildList<MealResponse, RecipeResponse>> getMealsByDayWithRecipes(Long dayId, String userId) {
        return getMealsByDay(dayId, userId)
//                .doOnNext(mealResponse -> log.error("mealResponse: {}", mealResponse))
                .flatMapSequential(mr -> recipeClient.getByIds(mr.getRecipes().stream().map(Object::toString).toList(), userId)
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

    private Mono<MealResponse> createModelWithAssociation(MealBody mealBody, String userId) {
        return
                super.createModel(mealBody, userId)
                        .flatMap(mealResponse ->
                                associativeEntityRepository.insertForMasterAndChildren(mealResponse.getId(), mealResponse.getRecipes())
                                        .thenReturn(mealResponse)
                        ).as(transactionalOperator::transactional);
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterPath = "dayId")
    public Mono<MealResponse> createModel(MealBody mealBody, String userId) {

        return
                recipeClient.verifyIds(mealBody.getRecipes().stream().map(Object::toString).toList(), userId)
                        .then(createModelWithAssociation(mealBody, userId));
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterPath = "dayId")
    public Mono<MealResponse> createModelCustomVerify(MealBody mealBody, List<Long> idsToNotVerify, String userId) {

        return
                recipeClient.verifyIds(mealBody.getRecipes().stream().filter(r -> !idsToNotVerify.contains(r)).map(Object::toString).toList(),
                                userId)
                        .then(createModelWithAssociation(mealBody, userId));
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterPath = "dayId", id = "#id")
    public Mono<MealResponse> updateModel(Long id, MealBody mealBody, String userId) {

        return
                getModelById(id, userId)
                        .flatMap(meal -> {
                            List<String> ids = mealBody.getRecipes().stream().filter(r -> !meal.getRecipes().contains(r))
                                    .map(Object::toString).toList();
                            return recipeClient.verifyIds(ids, userId)
                                    .then(super.updateModel(id, mealBody, userId))
                                    .flatMap(uM -> associativeEntityRepository.consensusChildrenForMaster(
                                                    uM.getId(), uM.getRecipes()
                                            ).thenReturn(uM)
                                    );
                        }).as(transactionalOperator::transactional);

    }

    @Override
    public Meal cloneModel(Meal meal) {
        return meal.clone();
    }

    @Override
    @RedisReactiveChildCacheEvict(key = CACHE_KEY_PATH, masterPath = "dayId", id = "#id")
    public Mono<MealResponse> deleteModel(Long id, String userId) {

        return
                planClient.getCountInParent(id, userId)
                        .flatMap(count -> {
                            if (count.getCount() > 0) {
                                return Mono.error(new SubEntityUsed("plan", id));
                            }
                            return super.deleteModel(id, userId);
                        });
    }

    @Getter
    @Component
    public static class MealServiceRedisCacheWrapper extends ManyToOneUserServiceImpl.ManyToOneUserServiceRedisCacheWrapper<Meal, MealBody, MealResponse, MealRepository, MealMapper> {

        public MealServiceRedisCacheWrapper(MealRepository modelRepository, MealMapper modelMapper, UserClient userClient) {
            super(modelRepository, modelMapper, "meal", userClient);

        }

        @Override
        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "entity.id")
        public Flux<MonthlyEntityGroup<MealResponse>> getModelGroupedByMonthBase(int month, UserDto userDto) {
            return super.getModelGroupedByMonthBase(month, userDto);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<Meal> findAllById(List<Long> ids) {
            return super.findAllById(ids);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Meal> getModel(Long id) {
            return super.getModel(id);
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Meal> getModelInternal(Long id) {
            return super.getModel(id);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserDto<MealResponse>> getModelByIdWithUserBase(UserDto authUser, Long id) {
            return super.getModelByIdWithUserBase(authUser, id);
        }

        @RedisReactiveChildCache(key = CACHE_KEY_PATH, idPath = "id", masterId = "#dayId")
        public Flux<Meal> findAllByDayIdCustomPeriodSort(Long dayId) {
            return modelRepository.findAllByDayIdCustomPeriodSort(dayId);
        }


    }


}

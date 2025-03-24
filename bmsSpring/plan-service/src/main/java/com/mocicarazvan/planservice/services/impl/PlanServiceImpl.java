package com.mocicarazvan.planservice.services.impl;

import com.mocicarazvan.planservice.clients.DayClient;
import com.mocicarazvan.planservice.clients.OrderClient;
import com.mocicarazvan.planservice.dtos.PlanBody;
import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.dtos.PlanResponseWithSimilarity;
import com.mocicarazvan.planservice.dtos.dayClient.DayResponse;
import com.mocicarazvan.planservice.dtos.dayClient.MealResponse;
import com.mocicarazvan.planservice.dtos.dayClient.RecipeResponse;
import com.mocicarazvan.planservice.dtos.dayClient.collect.FullDayResponse;
import com.mocicarazvan.planservice.dtos.dayClient.collect.FullMealResponse;
import com.mocicarazvan.planservice.enums.DayType;
import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.planservice.mappers.PlanMapper;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.planservice.repositories.ExtendedPlanRepository;
import com.mocicarazvan.planservice.repositories.PlanRepository;
import com.mocicarazvan.planservice.services.PlanService;
import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveApprovedCacheEvict;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveCacheEvict;
import com.mocicarazvan.rediscache.enums.BooleanEnum;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class PlanServiceImpl
        extends ApprovedServiceImpl<Plan, PlanBody, PlanResponse, PlanRepository, PlanMapper, PlanServiceImpl.PlanServiceRedisCacheWrapper>
        implements PlanService {


    private final ExtendedPlanRepository extendedPlanRepository;
    private final DayClient dayClient;
    private final OrderClient orderClient;
    private final RabbitMqApprovedSender<PlanResponse> rabbitMqSender;
    private final TransactionalOperator transactionalOperator;
    private final PlanEmbedServiceImpl planEmbedServiceImpl;

    public PlanServiceImpl(PlanRepository modelRepository, PlanMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, FileClient fileClient, ExtendedPlanRepository extendedPlanRepository, DayClient dayClient, OrderClient orderClient, RabbitMqApprovedSender<PlanResponse> rabbitMqSender, PlanServiceRedisCacheWrapper self, TransactionalOperator transactionalOperator, PlanEmbedServiceImpl planEmbedServiceImpl, RabbitMqUpdateDeleteService<Plan> rabbitMqUpdateDeleteService) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "plan", List.of("id", "userId", "type", "title", "createdAt", "updatedAt", "approved", "display"), entitiesUtils, fileClient, rabbitMqSender, self, rabbitMqUpdateDeleteService);
        this.extendedPlanRepository = extendedPlanRepository;
        this.dayClient = dayClient;
        this.orderClient = orderClient;
        this.rabbitMqSender = rabbitMqSender;
        this.transactionalOperator = transactionalOperator;
        this.planEmbedServiceImpl = planEmbedServiceImpl;
    }

    @Override
    public Mono<List<String>> seedEmbeddings() {
        return modelRepository.findAll()
                .flatMap(plan -> planEmbedServiceImpl.saveEmbedding(plan.getId(), plan.getTitle())
                        .then(Mono.just("Seeded embeddings for plan: " + plan.getId())))
                .collectList()
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUser(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds,
                                                                                              LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                              LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                              PageableBody pageableBody, String userId, Boolean admin) {
        return getPlansFiltered(title, approved, display, type, objective, excludeIds,
                createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                pageableBody, userId, admin)
                .flatMapSequential(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUserByIds(String title, DietType type, ObjectiveType objective, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, List<Long> ids, String userId) {

        return self.getPlansFilteredWithUserByIdsBase(title, type, objective,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, ids, allowedSortingFields)
                .flatMapSequential(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<PlanResponse>> getPlansFiltered(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                 LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, String userId, Boolean admin) {
        final boolean approvedNotNull = approved != null;

        return protectRoute(approvedNotNull, pageableBody, userId)
                .flatMapMany(pr -> self.getPlansFilteredBase(title, approved, display, type, objective, excludeIds,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pr, admin));
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredWithCount(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds,
                                                                                                   LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                                   PageableBody pageableBody, String userId, Boolean admin) {

        return getPlansFiltered(title, approved, display, type, objective, excludeIds,
                createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                pageableBody, userId, admin)
                .flatMapSequential(pr -> toResponseWithCount(userId, orderClient, pr));


    }

    @Override
    public Flux<PageableResponse<PlanResponse>> getPlansFilteredTrainer(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective,
                                                                        LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                        LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                        PageableBody pageableBody, String userId, Long trainerId) {

        return getModelsAuthor(trainerId, pageableBody, userId, pr ->
                self.getPlansFilteredTrainerBase(title, approved, display, type, objective, trainerId,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pr)
        );
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredTrainerWithCount(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective,
                                                                                                          LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                          LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                                          PageableBody pageableBody, String userId, Long trainerId) {
        return
                getPlansFilteredTrainer(title, approved, display, type, objective,
                        createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, userId, trainerId)
                        .flatMapSequential(pr -> toResponseWithCount(userId, orderClient, pr));
    }

    @Override
    public Flux<FullDayResponse> getFullDaysByPlan(Long id, String userId) {
        return getModelById(id, userId)
                .flatMapMany(model -> dayClient.getByIds(model.getDays()
                                .stream().map(Object::toString).toList(), userId)
                        .flatMap(dr -> getFullDayResponseMono(userId, dr)));

    }

    @Override
    public Flux<DayResponse> getDaysByPlan(Long id, String userId) {
        return getModelById(id, userId)
                .flatMapMany(model -> dayClient.getByIds(model.getDays()
                        .stream().map(Object::toString).toList(), userId));
    }

    @Override
    public Flux<FullDayResponse> getDaysByPlanInternal(Long id, String userId) {
        return self.getModelInternal(id)
                .flatMapMany(model -> dayClient.getByIds(model.getDays()
                                .stream().map(Object::toString).toList(), userId)
                        .flatMap(dr -> getFullDayResponseMono(userId, dr)));

    }

    private Mono<FullDayResponse> getFullDayResponseMono(String userId, DayResponse dr) {
        FullDayResponse fullDayResponse = FullDayResponse.fromDayResponse(dr);
        return dayClient.getMealsByDay(dr.getId().toString(), userId)
                .flatMap(mr -> {
                    FullMealResponse fullMealResponse = FullMealResponse.fromMealResponse(mr);
                    return dayClient.getRecipesByMeal(mr.getId().toString(), userId)
                            .collectList()
                            .doOnNext(fullMealResponse::setRecipes)
                            .thenReturn(fullMealResponse);
                }).collectList()
                .doOnNext(fullDayResponse::setMeals)
                .thenReturn(fullDayResponse);
    }

    @Override
    public Mono<FullDayResponse> getDayByPlan(Long id, Long dayId, String userId) {
        return getModelById(id, userId)
                .flatMap(model -> getSingleFullDayResponse(model.getDays(), dayId, userId));
    }

    @Override
    public Mono<FullDayResponse> getDayByPlanInternal(Long id, Long dayId, String userId) {
        return self.getModelInternal(id)
                .flatMap(model -> getSingleFullDayResponse(model.getDays(), dayId, userId));
    }

    private Mono<FullDayResponse> getSingleFullDayResponse(List<Long> modelDays, Long dayId, String userId) {
        if (!modelDays.contains(dayId)) {
            return Mono.error(new PrivateRouteException());
        }
        return dayClient.getByIds(List.of(String.valueOf(dayId)), userId)
                .flatMap(dr -> getFullDayResponseMono(userId, dr))
                .collectList()
                .flatMap(responses -> {
                    if (responses.isEmpty()) {
                        return Mono.error(new PrivateRouteException());
                    }
                    if (responses.size() > 1) {
                        return Mono.error(new IllegalArgumentException("More than one day found"));
                    }
                    return Mono.just(responses.getFirst());
                });
    }

    @Override
    public Mono<Void> validIds(List<Long> ids) {

        return
                this.validIds(ids, modelRepository, modelName)
                        .thenReturn(true)
                        .then();
    }

    @Override
    public Mono<PlanResponse> toggleDisplay(Long id, boolean display, String userId) {
        return
                userClient.getUser("", userId)
                        .flatMap(authUser -> getModel(id)
                                .flatMap(model -> isNotAuthor(model, authUser)
                                        .flatMap(isNotAuthor -> {
                                            if (isNotAuthor) {
                                                return Mono.error(new PrivateRouteException());
                                            }
                                            model.setDisplay(display);
                                            model.setUpdatedAt(LocalDateTime.now());
                                            return modelRepository.save(model)
                                                    .map(modelMapper::fromModelToResponse)
                                                    ;

                                        })
                                )
                        ).flatMap(r -> self.toggleDisplayEvict(id, display, userId, r))
                        .map(Pair::getFirst);
    }

    @Override
    public Mono<ResponseWithUserDto<PlanResponse>> getModelByIdWithUserInternal(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> self.getModel(id)
                        .flatMap(model -> userClient.getUser("", model.getUserId().toString())
                                .map(user ->
                                        ResponseWithUserDto.<PlanResponse>builder()
                                                .model(modelMapper.fromModelToResponse(model))
                                                .user(user)
                                                .build()
                                )
                        )
                );
    }

    @Override
    public Flux<PlanResponse> getModelsTrainerInternal(Long trainerId, String userId) {
        return
                self.getModelsTrainerInternalBase(trainerId);
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUserInternal(Long id, Long dayId, Long recipeId, String userId) {
        return self.getModelInternal(id)
                .map(Plan::getDays)
                .flatMap(days -> {
                    if (!days.contains(dayId)) {
                        return Mono.error(new IllegalActionException("Day not found in plan"));
                    }
                    return dayClient.getRecipeByIdWithUser(
                            String.valueOf(dayId), String.valueOf(recipeId), userId
                    );

                });
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<DayResponse>> getDayByIdWithUserInternal(Long id, Long dayId, String userId) {
        return self.getModelInternal(id)
                .map(Plan::getDays)
                .flatMap(days -> {
                    if (!days.contains(dayId)) {
                        return Mono.error(new IllegalActionException("Day not found in plan"));
                    }
                    return dayClient.getByIdWithUser(String.valueOf(dayId), userId);
                });
    }

    @Override
    public Flux<CustomEntityModel<MealResponse>> getMealsByDayInternal(Long id, Long dayId, String userId) {
        return self.getModelInternal(id)
                .map(Plan::getDays)
                .flatMapMany(days -> {
                    if (!days.contains(dayId)) {
                        return Flux.error(new IllegalActionException("Day not found in plan"));
                    }
                    return dayClient.getMealsByDayEntity(String.valueOf(dayId), userId);
                });
    }

    @Override
    public Mono<EntityCount> countInParent(Long childId) {
        return
                modelRepository.countInParent(childId)
                        .collectList()
                        .map(EntityCount::new);
    }

    @Override
    public Flux<PlanResponse> getModelsByIds(List<Long> ids) {
        return
                self.getModelsByIdsBase(ids);
    }

    @Override
    public Mono<PlanResponse> createModel(Flux<FilePart> images, PlanBody planBody, String userId, String clientId) {
        return

                dayClient.verifyIds(planBody.getDays()
                                        .stream().map(Object::toString).toList()
                                , userId)

                        .then(
                                super.createModel(images, planBody, userId, clientId))
                        .flatMap(plan -> planEmbedServiceImpl.saveEmbedding(plan.getId(), plan.getTitle()).thenReturn(plan))
                        .flatMap(self::createInvalidate)
                        .map(Pair::getFirst).as(transactionalOperator::transactional);
    }


    @Override
    @RedisReactiveCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<PlanResponse> updateModelWithImages(Flux<FilePart> images, Long id, PlanBody planBody, String userId, String clientId) {
        return
                verifyDayIds(id, planBody, userId)
                        .then(super.updateModelWithImages(images, id, planBody, userId, clientId));
    }


    @Override
    public Mono<Pair<PlanResponse, Boolean>> updateModelWithImagesGetOriginalApproved(Flux<FilePart> images, Long id, PlanBody planBody, String userId, String clientId) {

        return
                verifyDayIds(id, planBody, userId)
                        .then(super.updateModelWithImagesGetOriginalApproved(images, id, planBody, userId, clientId,
                                ((planBody1, s, plan) -> planEmbedServiceImpl.updateEmbeddingWithZip(planBody1.getTitle(), s, plan.getId(), modelRepository.save(plan)))
                        ))
                        .flatMap(self::updateDeleteInvalidate).as(transactionalOperator::transactional);
    }

    private Mono<Void> verifyDayIds(Long id, PlanBody planBody, String userId) {
        return getModelById(id, userId)
                .map(plan -> planBody.getDays().stream().filter(r -> !plan.getDays().contains(r))
                        .map(Object::toString).toList())
                .flatMap(ids -> dayClient.verifyIds(ids, userId));
    }

    @Override
    @RedisReactiveCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<PlanResponse> deleteModel(Long id, String userId) {
        return
                orderClient.getCountInParent(id, userId)
                        .flatMap(count -> {
                            if (count.getCount() > 0) {
                                return Mono.error(new SubEntityUsed("plan", id));
                            }
                            return super.deleteModel(id, userId);
                        });
    }

    @Override
    public Plan cloneModel(Plan plan) {
        return plan.clone();
    }


    @Override
    public Mono<Pair<PlanResponse, Boolean>> deleteModelGetOriginalApproved(Long id, String userId) {
        return orderClient.getCountInParent(id, userId)
                .flatMap(count -> {
                    if (count.getCount() > 0) {
                        return Mono.error(new SubEntityUsed("plan", id));
                    }
                    return super.deleteModelGetOriginalApproved(id, userId);
                }).flatMap(self::updateDeleteInvalidate);

    }

    @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
    @Override
    public Flux<PlanResponseWithSimilarity> getSimilarPlans(Long id, List<Long> excludeIds, int limit, Double minSimilarity) {
        return
                existsById(id).thenMany(
                        modelRepository.getSimilarPlans(id, excludeIds.toArray(Long[]::new), limit, minSimilarity)
                                .map(modelMapper::fromPlanWithSimilarityToResponse)

                );
    }

    public Mono<Void> existsById(Long id) {
        return modelRepository.existsById(id)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new NotFoundEntity("plan", id))
                ).then();
    }

    @Override
    @RedisReactiveCacheEvict(key = CACHE_KEY_PATH, id = "#id")
    public Mono<PlanResponse> reactToModel(Long id, String type, String userId) {
        return super.reactToModel(id, type, userId);
    }


    @Override
    public Flux<PageableResponse<CustomEntityModel<DayResponse>>> getDaysFilteredByPlanIdsIn(List<Long> plans,
                                                                                             String title, DayType type, List<Long> excludeIds,
                                                                                             LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                             LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                             PageableBody pageableBody, String userId, Boolean admin) {
        return self.findAllById(plans)
                .map(Plan::getDays)
                .flatMapIterable(longs -> longs)
                .collectList()
                .flatMapMany(daysIds -> dayClient.getDaysFilteredByIds(
                        title, type, Set.copyOf(daysIds).stream().toList(), excludeIds,
                        createdAtLowerBound, createdAtUpperBound,
                        updatedAtLowerBound, updatedAtUpperBound,
                        pageableBody, userId, admin
                ));
    }

    @Component
    @Getter
    public static class PlanServiceRedisCacheWrapper extends ApprovedServiceRedisCacheWrapper<Plan, PlanBody, PlanResponse, PlanRepository, PlanMapper> {

        private final ExtendedPlanRepository extendedPlanRepository;

        public PlanServiceRedisCacheWrapper(PlanRepository modelRepository, PlanMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, ExtendedPlanRepository extendedPlanRepository) {
            super(modelRepository, modelMapper, "plan", pageableUtils, userClient);
            this.extendedPlanRepository = extendedPlanRepository;
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        protected Mono<Boolean> existByIdApproved(Long id) {
            return modelRepository.existsByIdAndApprovedIsTrue(id);
        }


        @Override
        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, idPath = "entity.id", approved = BooleanEnum.NULL, forWhom = "0")
        public Flux<MonthlyEntityGroup<PlanResponse>> getModelGroupedByMonthBase(int month, UserDto userDto) {
            return super.getModelGroupedByMonthBase(month, userDto);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Plan> getModel(Long id) {
            return super.getModel(id);
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<Plan> getModelInternal(Long id) {
            return super.getModel(id);
        }


        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<Plan> findAllById(List<Long> ids) {
            return super.findAllById(ids);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserDto<PlanResponse>> getModelByIdWithUserBase(UserDto authUser, Long id) {
            return super.getModelByIdWithUserBase(authUser, id);
        }

        @Override
        @RedisReactiveCache(key = CACHE_KEY_PATH, id = "#id")
        public Mono<ResponseWithUserLikesAndDislikes<PlanResponse>> getModelByIdWithUserLikesAndDislikesBase(Long id, UserDto authUser) {
            return super.getModelByIdWithUserLikesAndDislikesBase(id, authUser);
        }

        @Override
        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, approvedArgumentPath = "#approved", idPath = "content.id")
        public Flux<PageableResponse<PlanResponse>> getModelsTitleBase(boolean approved, PageRequest pr, String newTitle) {
            return super.getModelsTitleBase(approved, pr, newTitle);
        }


        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, idPath = "content.id", approvedArgumentPath = "#approved", forWhom = "#admin?0:-1")
        public Flux<PageableResponse<PlanResponse>> getPlansFilteredBase(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                         LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageRequest pr, Boolean admin) {

            return
                    pageableUtils.createPageableResponse(
                            extendedPlanRepository.getPlansFiltered(title, approved, display, type, objective, pr, excludeIds,
                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound
                            ).map(modelMapper::fromModelToResponse),
                            extendedPlanRepository.countPlansFiltered(title, approved, display, type, objective, excludeIds,
                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound
                            ), pr
                    )
                    ;
        }

        // independent of approved
        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "content.id")
        public Flux<PageableResponse<PlanResponse>> getPlansFilteredWithUserByIdsBase(String title, DietType type, ObjectiveType objective,
                                                                                      LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                      LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                      PageableBody pageableBody, List<Long> ids, List<String> allowedSortingFields) {
            return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                    .then(pageableUtils.createPageRequest(pageableBody))
                    .flatMapMany(
                            pr ->
                                    pageableUtils.createPageableResponse(
                                            extendedPlanRepository.getPlansFilteredByIds(title, null, null, type, objective, ids,
                                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                                                    pr).map(modelMapper::fromModelToResponse),
                                            extendedPlanRepository.countPlansFilteredByIds(title, null, null, type, objective, ids,
                                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound
                                            ), pr
                                    )

                    );
        }

        @RedisReactiveApprovedCache(key = CACHE_KEY_PATH, idPath = "content.id", approvedArgumentPath = "#approved", forWhom = "#trainerId")
        public Flux<PageableResponse<PlanResponse>> getPlansFilteredTrainerBase(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, Long trainerId,
                                                                                LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageRequest pr
        ) {

            return
                    pageableUtils.createPageableResponse(
                            extendedPlanRepository.getPlansFilteredTrainer(title, approved, display, type, objective, trainerId,
                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound,
                                    pr).map(modelMapper::fromModelToResponse),
                            extendedPlanRepository.countPlansFilteredTrainer(title, approved, display, trainerId, type, objective,
                                    createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound
                            ), pr
                    );

        }

        @RedisReactiveApprovedCacheEvict(key = CACHE_KEY_PATH, id = "#id", forWhomPath = "#resp.userId")
        public Mono<Pair<PlanResponse, Boolean>> toggleDisplayEvict(Long id, boolean display, String userId, PlanResponse resp) {
            return Mono.just(Pair.of(resp, resp.isApproved()));
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<PlanResponse> getModelsTrainerInternalBase(Long trainerId) {
            return
                    modelRepository.findAllByUserId(trainerId)
                            .map(modelMapper::fromModelToResponse);
        }

        @RedisReactiveCache(key = CACHE_KEY_PATH, idPath = "id")
        public Flux<PlanResponse> getModelsByIdsBase(List<Long> ids) {
            return
                    modelRepository.findAllByIdIn(ids)
                            .map(modelMapper::fromModelToResponse);
        }

        @Override
        @RedisReactiveApprovedCacheEvict(key = CACHE_KEY_PATH, forWhomPath = "#r.userId")
        protected Mono<Pair<PlanResponse, Boolean>> createInvalidate(PlanResponse r) {
            return super.createInvalidate(r);
        }

        @Override
        @RedisReactiveApprovedCacheEvict(key = CACHE_KEY_PATH, id = "#p.getFirst().getId()", forWhomPath = "#p.getFirst().getUserId()")
        protected Mono<Pair<PlanResponse, Boolean>> updateDeleteInvalidate(Pair<PlanResponse, Boolean> p) {
            return super.updateDeleteInvalidate(p);
        }


    }

}

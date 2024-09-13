package com.mocicarazvan.planservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.planservice.clients.OrderClient;
import com.mocicarazvan.planservice.clients.DayClient;
import com.mocicarazvan.planservice.dtos.PlanBody;
import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.dtos.dayClient.DayResponse;
import com.mocicarazvan.planservice.dtos.dayClient.MealResponse;
import com.mocicarazvan.planservice.dtos.dayClient.RecipeResponse;
import com.mocicarazvan.planservice.dtos.dayClient.collect.FullDayResponse;
import com.mocicarazvan.planservice.dtos.dayClient.collect.FullMealResponse;
import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.planservice.mappers.PlanMapper;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.planservice.repositories.ExtendedPlanRepository;
import com.mocicarazvan.planservice.repositories.PlanRepository;
import com.mocicarazvan.planservice.services.PlanService;
import com.mocicarazvan.templatemodule.adapters.CacheApprovedFilteredToHandlerAdapter;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCache;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheApproveFilterKey;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSenderWrapper;
import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.function.Function10;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function7;
import org.jooq.lambda.function.Function9;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Service
@Slf4j
public class PlanServiceImpl
        extends ApprovedServiceImpl<Plan, PlanBody, PlanResponse, PlanRepository, PlanMapper>
        implements PlanService {


    private final ExtendedPlanRepository extendedPlanRepository;
    private final DayClient dayClient;
    private final OrderClient orderClient;
    private final PlanServiceCacheHandler planServiceCacheHandler;
    private final RabbitMqApprovedSenderWrapper<PlanResponse> rabbitMqSender;


    public PlanServiceImpl(PlanRepository modelRepository, PlanMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, FileClient fileClient, ObjectMapper objectMapper, ExtendedPlanRepository extendedPlanRepository, DayClient dayClient, OrderClient orderClient, PlanServiceCacheHandler planServiceCacheHandler, RabbitMqApprovedSenderWrapper<PlanResponse> rabbitMqSender) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "plan", List.of("id", "userId", "type", "title", "createdAt", "updatedAt", "approved", "display"), entitiesUtils, fileClient, objectMapper, planServiceCacheHandler, rabbitMqSender);
        this.extendedPlanRepository = extendedPlanRepository;
        this.dayClient = dayClient;
        this.orderClient = orderClient;
        this.planServiceCacheHandler = planServiceCacheHandler;
        this.rabbitMqSender = rabbitMqSender;
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUser(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds, PageableBody pageableBody, String userId, Boolean admin) {
        return getPlansFiltered(title, approved, display, type, objective, excludeIds, pageableBody, userId, admin)
                .concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUserByIds(String title, DietType type, ObjectiveType objective, PageableBody pageableBody, List<Long> ids, String userId) {
        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(
                        pr ->
                                planServiceCacheHandler.getPlansFilteredWithUserByIdsPersist.apply(
                                        pageableUtils.createPageableResponse(
                                                extendedPlanRepository.getPlansFilteredByIds(title, null, null, type, objective, ids, pr).map(modelMapper::fromModelToResponse),
                                                extendedPlanRepository.countPlansFilteredByIds(title, null, null, type, objective, ids), pr
                                        ), title, type, objective, pageableBody, ids, userId)

                ).concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<PlanResponse>> getPlansFiltered(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds, PageableBody pageableBody, String userId, Boolean admin) {
        final boolean approvedNotNull = approved != null;

        return protectRoute(approvedNotNull, pageableBody, userId)
                .flatMapMany(pr ->
                        planServiceCacheHandler.getPlansFilteredPersist.apply(
                                pageableUtils.createPageableResponse(
                                        extendedPlanRepository.getPlansFiltered(title, approved, display, type, objective, pr, excludeIds).map(modelMapper::fromModelToResponse),
                                        extendedPlanRepository.countPlansFiltered(title, approved, display, type, objective, excludeIds), pr
                                ), title, approved, display, type, objective, excludeIds, pageableBody, userId, admin)

                );
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredWithCount(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds, PageableBody pageableBody, String userId, Boolean admin) {

        return getPlansFiltered(title, approved, display, type, objective, excludeIds, pageableBody, userId, admin)
                .concatMap(pr -> toResponseWithCount(userId, orderClient, pr));


    }

    @Override
    public Flux<PageableResponse<PlanResponse>> getPlansFilteredTrainer(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, PageableBody pageableBody, String userId, Long trainerId) {
        return getModelsAuthor(trainerId, pageableBody, userId, pr ->
                planServiceCacheHandler.getPlansFilteredTrainerPersist.apply(
                        pageableUtils.createPageableResponse(
                                extendedPlanRepository.getPlansFilteredTrainer(title, approved, display, type, objective, trainerId, pr).map(modelMapper::fromModelToResponse),
                                extendedPlanRepository.countPlansFilteredTrainer(title, approved, display, trainerId, type, objective), pr
                        ), title, approved, display, type, objective, pageableBody, userId, trainerId)
        );
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredTrainerWithCount(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, PageableBody pageableBody, String userId, Long trainerId) {
        return
                getPlansFilteredTrainer(title, approved, display, type, objective, pageableBody, userId, trainerId)
                        .concatMap(pr -> toResponseWithCount(userId, orderClient, pr));
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
        return getModel(id)
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
        return getModel(id)
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
                planServiceCacheHandler.validIdsPersist.apply(
                                this.validIds(ids, modelRepository, modelName)
                                        .thenReturn(true), ids)
                        .then();
    }

    @Override
    public Mono<PlanResponse> toggleDisplay(Long id, boolean display, String userId) {
        return
                planServiceCacheHandler.getUpdateModelGetOriginalApprovedInvalidate().apply(
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
                                                                    .map(p -> Pair.of(p, p.isApproved()))
                                                                    ;

                                                        })
                                                )
                                        ), id, new PlanBody(), userId)
                        .map(Pair::getFirst);
    }

//    @Override
//    public Mono<ResponseWithUserDtoEntity<DayResponse>> getDayInternalByPlan(Long id, Long dayId, String userId) {
//        return getModel(id)
//                .flatMap(model -> {
//                            if (!model.getDays().contains(dayId)) {
//                                return Mono.error(new PrivateRouteException());
//                            }
//                            return dayClient.getByIdWithUser(String.valueOf(dayId), userId);
//                        }
//                );
//    }

    @Override
    public Mono<ResponseWithUserDto<PlanResponse>> getModelByIdWithUserInternal(Long id, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
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
                planServiceCacheHandler.getModelsTrainerInternalPersist.apply(
                        modelRepository.findAllByUserId(trainerId)
                                .map(modelMapper::fromModelToResponse), trainerId);
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUserInternal(Long id, Long dayId, Long recipeId, String userId) {
        return getModel(id)
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
        return getModel(id)
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
        return getModel(id)
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
//        return modelRepository.findAllByIdInAndApprovedTrue(ids)
//                .map(modelMapper::fromModelToResponse);
        return
                planServiceCacheHandler.getModelsByIdsPersist.apply(
                        modelRepository.findAllByIdIn(ids)
                                .map(modelMapper::fromModelToResponse), ids);
    }

    @Override
    public Mono<PlanResponse> createModel(Flux<FilePart> images, PlanBody planBody, String userId, String clientId) {
        return

                planServiceCacheHandler.getCreateModelInvalidate().apply(
                        dayClient.verifyIds(planBody.getDays()
                                        .stream().map(Object::toString).toList()
                                , userId).then(
                                super.createModel(images, planBody, userId, clientId)), planBody, userId);
    }

    @Override
    public Mono<PlanResponse> updateModelWithImages(Flux<FilePart> images, Long id, PlanBody planBody, String userId, String clientId) {
        return
                planServiceCacheHandler.getUpdateModelInvalidate().apply(
                        verifyDayIds(id, planBody, userId)
                                .then(super.updateModelWithImages(images, id, planBody, userId, clientId)), id, planBody, userId);
//                super.updateModelWithImages(images, id, planBody, userId);
    }


    @Override
    public Mono<Pair<PlanResponse, Boolean>> updateModelWithImagesGetOriginalApproved(Flux<FilePart> images, Long id, PlanBody planBody, String userId, String clientId) {
        return
                planServiceCacheHandler.getUpdateModelGetOriginalApprovedInvalidate().apply(
                        verifyDayIds(id, planBody, userId)
                                .then(super.updateModelWithImagesGetOriginalApproved(images, id, planBody, userId, clientId)), id, planBody, userId);
    }

    private Mono<Void> verifyDayIds(Long id, PlanBody planBody, String userId) {
        return getModelById(id, userId)
                .map(plan -> planBody.getDays().stream().filter(r -> !plan.getDays().contains(r))
                        .map(Object::toString).toList())
                .flatMap(ids -> dayClient.verifyIds(ids, userId));
    }

    @Override
    public Mono<PlanResponse> deleteModel(Long id, String userId) {
        return
                planServiceCacheHandler.getDeleteModelInvalidate().apply(
                        orderClient.getCountInParent(id, userId)
                                .flatMap(count -> {
                                    if (count.getCount() > 0) {
                                        return Mono.error(new SubEntityUsed("plan", id));
                                    }
                                    return super.deleteModel(id, userId);
                                }), id, userId);
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Component
    public static class PlanServiceCacheHandler
            extends ApprovedServiceImpl.ApprovedServiceCacheHandler<Plan, PlanBody, PlanResponse> {
        private final FilteredListCaffeineCacheApproveFilterKey<PlanResponse> cacheFilterList;

        Function10<Flux<PageableResponse<PlanResponse>>, String, Boolean, Boolean, DietType, ObjectiveType, List<Long>, PageableBody, String, Boolean, Flux<PageableResponse<PlanResponse>>>
                getPlansFilteredPersist;
        Function7<Flux<PageableResponse<PlanResponse>>, String, DietType, ObjectiveType, PageableBody, List<Long>, String, Flux<PageableResponse<PlanResponse>>>
                getPlansFilteredWithUserByIdsPersist;
        Function9<Flux<PageableResponse<PlanResponse>>, String, Boolean, Boolean, DietType, ObjectiveType, PageableBody, String, Long, Flux<PageableResponse<PlanResponse>>>
                getPlansFilteredTrainerPersist;
        Function2<Mono<Boolean>, List<Long>, Mono<Boolean>> validIdsPersist;
        Function2<Flux<PlanResponse>, List<Long>, Flux<PlanResponse>> getModelsByIdsPersist;
        Function2<Flux<PlanResponse>, Long, Flux<PlanResponse>> getModelsTrainerInternalPersist;

        public PlanServiceCacheHandler(FilteredListCaffeineCacheApproveFilterKey<PlanResponse> cacheFilterList) {
            super();
            this.cacheFilterList = cacheFilterList;
            CacheApprovedFilteredToHandlerAdapter.convert(cacheFilterList, this);

            this.getPlansFilteredPersist = (flux, title, approved, display, type, objective, excludeIds, pageableBody, userId, admin) -> {
                FilterKeyType.KeyRouteType keyRouteType = Boolean.TRUE.equals(admin) ? FilterKeyType.KeyRouteType.createForAdmin() : FilterKeyType.KeyRouteType.createForPublic();
                return cacheFilterList.getExtraUniqueFluxCache(
                        EntitiesUtils.getListOfNotNullObjects(title, approved, display, type, objective, excludeIds, pageableBody, admin),
                        "/getPlansFiltered",
                        (mwr) -> mwr.getContent().getId(),
                        keyRouteType,
                        approved,
                        flux
                );
            };
            this.getPlansFilteredWithUserByIdsPersist = (flux, title, type, objective, pageableBody, ids, userId) ->
                    cacheFilterList.getExtraUniqueFluxCacheIndependent(
                            EntitiesUtils.getListOfNotNullObjects(title, type, objective, pageableBody, ids),
                            "/getPlansFilteredWithUserByIds",
                            (mwr) -> mwr.getContent().getId(),
                            flux
                    );

            this.getPlansFilteredTrainerPersist = (flux, title, approved, display, type, objective, pageableBody, userId, trainerId) ->
                    cacheFilterList.getExtraUniqueCacheForTrainer(
                            EntitiesUtils.getListOfNotNullObjects(title, approved, display, type, objective, pageableBody, trainerId),
                            trainerId,
                            "/getPlansFilteredTrainer" + trainerId,
                            (mwr) -> mwr.getContent().getId(),
                            approved,
                            flux
                    );

            this.validIdsPersist = (mono, ids) -> cacheFilterList.getExtraUniqueMonoCacheIdListIndependent(
                    EntitiesUtils.getListOfNotNullObjects(ids),
                    "validIdsPersist" + ids,
                    ids,
                    mono
            );


            this.getModelsByIdsPersist = (flux, ids) -> cacheFilterList.getExtraUniqueFluxCacheIndependent(
                    EntitiesUtils.getListOfNotNullObjects(ids),
                    "getModelsByIdsPersist" + ids,
                    IdGenerateDto::getId,
                    flux
            );
            this.getModelsTrainerInternalPersist = (flux, trainerId) -> cacheFilterList.getExtraUniqueFluxCacheIndependent(
                    EntitiesUtils.getListOfNotNullObjects(trainerId),
                    "getModelsTrainerInternalPersist" + trainerId,
                    IdGenerateDto::getId,
                    flux
            );

        }


    }
}

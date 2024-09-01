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
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.exceptions.action.IllegalActionException;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PlanServiceImpl
        extends ApprovedServiceImpl<Plan, PlanBody, PlanResponse, PlanRepository, PlanMapper>
        implements PlanService {


    private final ExtendedPlanRepository extendedPlanRepository;

    private final DayClient dayClient;
    private final OrderClient orderClient;

    public PlanServiceImpl(PlanRepository modelRepository, PlanMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, FileClient fileClient, ObjectMapper objectMapper, ExtendedPlanRepository extendedPlanRepository, DayClient dayClient, OrderClient orderClient) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "plan", List.of("id", "userId", "type", "title", "createdAt", "updatedAt", "approved", "display"), entitiesUtils, fileClient, objectMapper);
        this.extendedPlanRepository = extendedPlanRepository;
        this.dayClient = dayClient;
        this.orderClient = orderClient;
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUser(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds, PageableBody pageableBody, String userId) {
        return getPlansFiltered(title, approved, display, type, objective, excludeIds, pageableBody, userId)
                .concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUserByIds(String title, DietType type, ObjectiveType objective, PageableBody pageableBody, List<Long> ids, String userId) {
        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(
                        pr -> pageableUtils.createPageableResponse(
                                extendedPlanRepository.getPlansFilteredByIds(title, null, null, type, objective, ids, pr).map(modelMapper::fromModelToResponse),
                                extendedPlanRepository.countPlansFilteredByIds(title, null, null, type, objective, ids), pr
                        )

                ).concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<PlanResponse>> getPlansFiltered(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds, PageableBody pageableBody, String userId) {
        final boolean approvedNotNull = approved != null;

        return protectRoute(approvedNotNull, pageableBody, userId)
                .flatMapMany(pr -> pageableUtils.createPageableResponse(
                        extendedPlanRepository.getPlansFiltered(title, approved, display, type, objective, pr, excludeIds).map(modelMapper::fromModelToResponse),
                        extendedPlanRepository.countPlansFiltered(title, approved, display, type, objective, excludeIds), pr
                ));
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredWithCount(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds, PageableBody pageableBody, String userId) {
        return getPlansFiltered(title, approved, display, type, objective, excludeIds, pageableBody, userId)
                .concatMap(pr -> toResponseWithCount(userId, orderClient, pr));
    }

    @Override
    public Flux<PageableResponse<PlanResponse>> getPlansFilteredTrainer(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, PageableBody pageableBody, String userId, Long trainerId) {
        return getModelsAuthor(trainerId, pageableBody, userId, pr ->
                pageableUtils.createPageableResponse(
                        extendedPlanRepository.getPlansFilteredTrainer(title, approved, display, type, objective, trainerId, pr).map(modelMapper::fromModelToResponse),
                        extendedPlanRepository.countPlansFilteredTrainer(title, approved, display, trainerId, type, objective), pr
                ));
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredTrainerWithCount(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, PageableBody pageableBody, String userId, Long trainerId) {
        return getPlansFilteredTrainer(title, approved, display, type, objective, pageableBody, userId, trainerId)
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
        return this.validIds(ids, modelRepository, modelName);
    }

    @Override
    public Mono<PlanResponse> toggleDisplay(Long id, boolean display, String userId) {
        return userClient.getUser("", userId)
                .flatMap(authUser -> getModel(id)
                        .flatMap(model -> isNotAuthor(model, authUser)
                                .flatMap(isNotAuthor -> {
                                    if (isNotAuthor) {
                                        return Mono.error(new PrivateRouteException());
                                    }
                                    model.setDisplay(display);
                                    model.setUpdatedAt(LocalDateTime.now());
                                    return modelRepository.save(model)
                                            .map(modelMapper::fromModelToResponse);

                                })
                        )
                );
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
        return modelRepository.findAllByUserId(trainerId)
                .map(modelMapper::fromModelToResponse);
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
        return modelRepository.countInParent(childId)
                .map(EntityCount::new);
    }

    @Override
    public Flux<PlanResponse> getModelsByIds(List<Long> ids) {
//        return modelRepository.findAllByIdInAndApprovedTrue(ids)
//                .map(modelMapper::fromModelToResponse);
        return modelRepository.findAllByIdIn(ids)
                .map(modelMapper::fromModelToResponse);
    }

    @Override
    public Mono<PlanResponse> createModel(Flux<FilePart> images, PlanBody planBody, String userId, String clientId) {
        return
                dayClient.verifyIds(planBody.getDays()
                                .stream().map(Object::toString).toList()
                        , userId).then(
                        super.createModel(images, planBody, userId, clientId));
    }

    @Override
    public Mono<PlanResponse> updateModelWithImages(Flux<FilePart> images, Long id, PlanBody planBody, String userId, String clientId) {
        return
                getModelById(id, userId)
                        .map(plan -> planBody.getDays().stream().filter(r -> !plan.getDays().contains(r))
                                .map(Object::toString).toList())
                        .flatMap(ids -> dayClient.verifyIds(ids, userId))
                        .then(super.updateModelWithImages(images, id, planBody, userId, clientId));

//                super.updateModelWithImages(images, id, planBody, userId);
    }

    @Override
    public Mono<PlanResponse> deleteModel(Long id, String userId) {
        return orderClient.getCountInParent(id, userId)
                .flatMap(count -> {
                    if (count.getCount() > 0) {
                        return Mono.error(new SubEntityUsed("plan", id));
                    }
                    return super.deleteModel(id, userId);
                });
    }
}

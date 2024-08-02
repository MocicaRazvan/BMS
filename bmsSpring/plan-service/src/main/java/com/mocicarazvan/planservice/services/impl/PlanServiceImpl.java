package com.mocicarazvan.planservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.planservice.clients.OrderClient;
import com.mocicarazvan.planservice.clients.RecipeClient;
import com.mocicarazvan.planservice.dtos.PlanBody;
import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.mappers.PlanMapper;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.planservice.repositories.ExtendedPlanRepository;
import com.mocicarazvan.planservice.repositories.PlanRepository;
import com.mocicarazvan.planservice.services.PlanService;
import com.mocicarazvan.templatemodule.clients.FileClient;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.*;
import com.mocicarazvan.templatemodule.exceptions.action.PrivateRouteException;
import com.mocicarazvan.templatemodule.exceptions.action.SubEntityUsed;
import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.extern.slf4j.Slf4j;
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

    private final RecipeClient recipeClient;
    private final OrderClient orderClient;

    public PlanServiceImpl(PlanRepository modelRepository, PlanMapper modelMapper, PageableUtilsCustom pageableUtils, UserClient userClient, EntitiesUtils entitiesUtils, FileClient fileClient, ObjectMapper objectMapper, ExtendedPlanRepository extendedPlanRepository, RecipeClient recipeClient, OrderClient orderClient) {
        super(modelRepository, modelMapper, pageableUtils, userClient, "plan", List.of("id", "userId", "type", "title", "createdAt", "updatedAt", "approved", "display"), entitiesUtils, fileClient, objectMapper);
        this.extendedPlanRepository = extendedPlanRepository;
        this.recipeClient = recipeClient;
        this.orderClient = orderClient;
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUser(String title, Boolean approved, Boolean display, DietType type, List<Long> excludeIds, PageableBody pageableBody, String userId) {
        return getPlansFiltered(title, approved, display, type, excludeIds, pageableBody, userId)
                .concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUserByIds(String title, DietType type, PageableBody pageableBody, List<Long> ids, String userId) {
        return pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedSortingFields)
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(
                        pr -> pageableUtils.createPageableResponse(
                                extendedPlanRepository.getPlansFilteredByIds(title, null, null, type, ids, pr).map(modelMapper::fromModelToResponse),
                                extendedPlanRepository.countPlansFilteredByIds(title, null, null, type, ids), pr
                        )

                ).concatMap(this::getPageableWithUser);
    }

    @Override
    public Flux<PageableResponse<PlanResponse>> getPlansFiltered(String title, Boolean approved, Boolean display, DietType type, List<Long> excludeIds, PageableBody pageableBody, String userId) {
        final boolean approvedNotNull = approved != null;

        return protectRoute(approvedNotNull, pageableBody, userId)
                .flatMapMany(pr -> pageableUtils.createPageableResponse(
                        extendedPlanRepository.getPlansFiltered(title, approved, display, type, pr, excludeIds).map(modelMapper::fromModelToResponse),
                        extendedPlanRepository.countPlansFiltered(title, approved, display, type, excludeIds), pr
                ));
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredWithCount(String title, Boolean approved, Boolean display, DietType type, List<Long> excludeIds, PageableBody pageableBody, String userId) {
        return getPlansFiltered(title, approved, display, type, excludeIds, pageableBody, userId)
                .concatMap(pr -> toResponseWithCount(userId, orderClient, pr));
    }

    @Override
    public Flux<PageableResponse<PlanResponse>> getPlansFilteredTrainer(String title, Boolean approved, Boolean display, DietType type, PageableBody pageableBody, String userId, Long trainerId) {
        return getModelsAuthor(trainerId, pageableBody, userId, pr ->
                pageableUtils.createPageableResponse(
                        extendedPlanRepository.getPlansFilteredTrainer(title, approved, display, type, trainerId, pr).map(modelMapper::fromModelToResponse),
                        extendedPlanRepository.countPlansFilteredTrainer(title, approved, display, trainerId, type), pr
                ));
    }

    @Override
    public Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredTrainerWithCount(String title, Boolean approved, Boolean display, DietType type, PageableBody pageableBody, String userId, Long trainerId) {
        return getPlansFilteredTrainer(title, approved, display, type, pageableBody, userId, trainerId)
                .concatMap(pr -> toResponseWithCount(userId, orderClient, pr));
    }

    @Override
    public Flux<RecipeResponse> getRecipesByPlan(Long id, String userId) {
        return getModel(id)
                .flatMapMany(model -> recipeClient.getByIds(model.getRecipes()
                        .stream().map(Object::toString).toList(), userId));

    }

    @Override
    public Mono<RecipeResponse> getRecipeByPlan(Long id, Long recipeId, String userId) {
        return getModel(id)
                .flatMap(model -> {
                    if (!model.getRecipes().contains(recipeId)) {
                        return Mono.error(new PrivateRouteException());
                    }
                    return recipeClient.getByIds(List.of(String.valueOf(recipeId)), userId)
                            .collectList()
                            .flatMap(responses -> {
                                if (responses.isEmpty()) {
                                    return Mono.error(new PrivateRouteException());
                                }
                                if (responses.size() > 1) {
                                    return Mono.error(new IllegalArgumentException("More than one recipe found"));
                                }
                                return Mono.just(responses.get(0));
                            });

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

    @Override
    public Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeInternalByPlan(Long id, Long recipeId, String userId) {
        return getModel(id)
                .flatMap(model -> {
                            if (!model.getRecipes().contains(recipeId)) {
                                return Mono.error(new PrivateRouteException());
                            }
                            return recipeClient.getByIdWithUser(String.valueOf(recipeId), userId);
                        }
                );
    }

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
    public Mono<PlanResponse> createModel(Flux<FilePart> images, PlanBody planBody, String userId) {
        return
                recipeClient.verifyIds(planBody.getRecipes()
                                .stream().map(Object::toString).toList()
                        , userId).then(
                        super.createModel(images, planBody, userId));
    }

    @Override
    public Mono<PlanResponse> updateModelWithImages(Flux<FilePart> images, Long id, PlanBody planBody, String userId) {
        return
                getModelById(id, userId)
                        .map(plan -> planBody.getRecipes().stream().filter(r -> !plan.getRecipes().contains(r))
                                .map(Object::toString).toList())
                        .flatMap(ids -> recipeClient.verifyIds(ids, userId))
                        .then(super.updateModelWithImages(images, id, planBody, userId));

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

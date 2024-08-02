package com.mocicarazvan.planservice.services;

import com.mocicarazvan.planservice.dtos.PlanBody;
import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.mappers.PlanMapper;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.planservice.repositories.PlanRepository;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.services.ApprovedService;
import com.mocicarazvan.templatemodule.services.CountInParentService;
import com.mocicarazvan.templatemodule.services.ValidIds;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PlanService
        extends ApprovedService<Plan, PlanBody, PlanResponse, PlanRepository, PlanMapper>
        , CountInParentService, ValidIds<Plan, PlanRepository, PlanResponse> {

    Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUser(String title, Boolean approved, Boolean display, DietType type, List<Long> excludeIds, PageableBody pageableBody, String userId);

    Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUserByIds(String title, DietType type, PageableBody pageableBody, List<Long> ids, String userId);

    Flux<PageableResponse<PlanResponse>> getPlansFiltered(String title, Boolean approved, Boolean display, DietType type, List<Long> excludeIds, PageableBody pageableBody, String userId);

    Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredWithCount(String title, Boolean approved, Boolean display, DietType type, List<Long> excludeIds, PageableBody pageableBody, String userId);

    Flux<PageableResponse<PlanResponse>> getPlansFilteredTrainer(String title, Boolean approved, Boolean display, DietType type, PageableBody pageableBody, String userId, Long trainerId);

    Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredTrainerWithCount(String title, Boolean approved, Boolean display, DietType type, PageableBody pageableBody, String userId, Long trainerId);

    Flux<RecipeResponse> getRecipesByPlan(Long id, String userId);

    Mono<RecipeResponse> getRecipeByPlan(Long id, Long recipeId, String userId);

    Mono<Void> validIds(List<Long> ids);

    Mono<PlanResponse> toggleDisplay(Long id, boolean display, String userId);

    Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeInternalByPlan(Long id, Long recipeId, String userId);

    Mono<ResponseWithUserDto<PlanResponse>> getModelByIdWithUserInternal(Long id, String userId);

    Flux<PlanResponse> getModelsTrainerInternal(Long trainerId, String userId);
}

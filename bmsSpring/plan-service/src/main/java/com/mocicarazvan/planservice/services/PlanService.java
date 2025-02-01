package com.mocicarazvan.planservice.services;

import com.mocicarazvan.planservice.dtos.PlanBody;
import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.dtos.PlanResponseWithSimilarity;
import com.mocicarazvan.planservice.dtos.dayClient.DayResponse;
import com.mocicarazvan.planservice.dtos.dayClient.MealResponse;
import com.mocicarazvan.planservice.dtos.dayClient.RecipeResponse;
import com.mocicarazvan.planservice.dtos.dayClient.collect.FullDayResponse;
import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.planservice.mappers.PlanMapper;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.planservice.repositories.PlanRepository;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithEntityCount;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.services.ApprovedService;
import com.mocicarazvan.templatemodule.services.CountInParentService;
import com.mocicarazvan.templatemodule.services.ValidIds;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface PlanService
        extends ApprovedService<Plan, PlanBody, PlanResponse, PlanRepository, PlanMapper>
        , CountInParentService, ValidIds<Plan, PlanRepository, PlanResponse> {

    Mono<List<String>> seedEmbeddings();

    Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUser(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds,
                                                                                       LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                       LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                       PageableBody pageableBody, String userId, Boolean admin);

    Flux<PageableResponse<ResponseWithUserDto<PlanResponse>>> getPlansFilteredWithUserByIds(String title, DietType type, ObjectiveType objective, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                            LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, List<Long> ids, String userId);

    Flux<PageableResponse<PlanResponse>> getPlansFiltered(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                          LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, String userId, Boolean admin);

    Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredWithCount(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective, List<Long> excludeIds,
                                                                                            LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                            LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                            PageableBody pageableBody, String userId, Boolean admin);

    Flux<PageableResponse<PlanResponse>> getPlansFilteredTrainer(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective,
                                                                 LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                 LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                 PageableBody pageableBody, String userId, Long trainerId);

    Flux<PageableResponse<ResponseWithEntityCount<PlanResponse>>> getPlansFilteredTrainerWithCount(String title, Boolean approved, Boolean display, DietType type, ObjectiveType objective,
                                                                                                   LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                                                                                   PageableBody pageableBody, String userId, Long trainerId);

    Flux<FullDayResponse> getFullDaysByPlan(Long id, String userId);

    Flux<DayResponse> getDaysByPlan(Long id, String userId);

    Mono<FullDayResponse> getDayByPlan(Long id, Long dayId, String userId);

    Flux<FullDayResponse> getDaysByPlanInternal(Long id, String userId);

    Mono<FullDayResponse> getDayByPlanInternal(Long id, Long dayId, String userId);

    Mono<Void> validIds(List<Long> ids);

    Mono<PlanResponse> toggleDisplay(Long id, boolean display, String userId);

//    Mono<ResponseWithUserDtoEntity<DayResponse>> getDayInternalByPlan(Long id, Long dayId, String userId);

    Mono<ResponseWithUserDto<PlanResponse>> getModelByIdWithUserInternal(Long id, String userId);

    Flux<PlanResponse> getModelsTrainerInternal(Long trainerId, String userId);

    Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUserInternal(Long id, Long dayId, Long recipeId, String userId);

    Mono<ResponseWithUserDtoEntity<DayResponse>> getDayByIdWithUserInternal(Long id, Long dayId, String userId);

    Flux<CustomEntityModel<MealResponse>> getMealsByDayInternal(Long id, Long dayId, String userId);

    Flux<PlanResponseWithSimilarity> getSimilarPlans(Long id, List<Long> excludeIds, int limit, Double minSimilarity);
}

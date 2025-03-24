package com.mocicarazvan.orderservice.services;

import com.mocicarazvan.orderservice.dtos.*;
import com.mocicarazvan.orderservice.dtos.clients.DayResponse;
import com.mocicarazvan.orderservice.dtos.clients.MealResponse;
import com.mocicarazvan.orderservice.dtos.clients.PlanResponse;
import com.mocicarazvan.orderservice.dtos.clients.RecipeResponse;
import com.mocicarazvan.orderservice.dtos.clients.collect.FullDayResponse;
import com.mocicarazvan.orderservice.enums.DayType;
import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.orderservice.enums.ObjectiveType;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.services.CountInParentService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface OrderService extends CountInParentService {

    Mono<SessionResponse> checkoutHosted(CheckoutRequestBody checkoutRequestBody, String userId);

    Mono<String> handleWebhook(String payload, String sigHeader);

    Mono<InvoicesResponse> getInvoices(String userId, PageableBody pageableBody);

    Flux<UserSubscriptionDto> getSubscriptions(String userId);

    Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getPlansForUser(String title, DietType dietType, ObjectiveType objective, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, PageableBody pageableBody, String userId);

    Mono<FullDayResponse> getDayByPlanForUser(Long id, Long dayId, String userId);

    Mono<ResponseWithUserDtoEntity<PlanResponse>> getPlanByIdForUser(Long id, String userId);

    Mono<CustomInvoiceDto> getInvoiceById(String id, String userId);

    Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getPlansByOrder(Long orderId, String userId);


    Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUser(Long planId, Long dayId, Long recipeId, String userId);

    Mono<ResponseWithUserDtoEntity<DayResponse>> getDayByIdWithUser(Long planId, Long dayId, String userId);

    Flux<CustomEntityModel<MealResponse>> getMealsByDayInternal(Long planId, Long dayId, String userId);


    Mono<String> seedPlanOrders(String userId);

    Flux<PageableResponse<CustomEntityModel<DayResponse>>> getDaysFilteredByPlanIdsIn(
            String title, DayType type, List<Long> excludeIds,
            LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
            LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
            PageableBody pageableBody, String userId, Boolean admin);
}

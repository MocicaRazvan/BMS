package com.mocicarazvan.orderservice.services;

import com.mocicarazvan.orderservice.dtos.*;
import com.mocicarazvan.orderservice.dtos.clients.DayResponse;
import com.mocicarazvan.orderservice.dtos.clients.MealResponse;
import com.mocicarazvan.orderservice.dtos.clients.PlanResponse;
import com.mocicarazvan.orderservice.dtos.clients.RecipeResponse;
import com.mocicarazvan.orderservice.dtos.clients.collect.FullDayResponse;
import com.mocicarazvan.orderservice.dtos.summaries.*;
import com.mocicarazvan.orderservice.enums.CountrySummaryType;
import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.orderservice.enums.ObjectiveType;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.services.CountInParentService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

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

    Flux<MonthlyEntityGroup<OrderDto>> getOrdersGroupedByMonth(int month, String userId);

    Flux<MonthlyOrderSummary> getOrdersSummaryByMonth(LocalDate from, LocalDate to, String userId);

    Flux<TopUsersSummary> getTopUsersSummary(LocalDate from, LocalDate to, int top);

    Flux<TopPlansSummary> getTopPlansSummary(LocalDate from, LocalDate to, int top);

    Flux<TopPlansSummary> getTopPlansSummaryTrainer(LocalDate from, LocalDate to, int top, Long trainerId, String userId);

    Flux<MonthlyOrderSummary> getTrainerOrdersSummaryByMonth(LocalDate from, LocalDate to, Long trainerId, String userId);

    Flux<DailyOrderSummary> getOrdersSummaryByDay(LocalDate from, LocalDate to, String userId);

    Flux<DailyOrderSummary> getTrainerOrdersSummaryByDay(LocalDate from, LocalDate to, Long trainerId, String userId);

    Flux<CountryOrderSummary> getOrdersSummaryByCountry(CountrySummaryType type, LocalDate from, LocalDate to);

    Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByIdWithUser(Long planId, Long dayId, Long recipeId, String userId);

    Mono<ResponseWithUserDtoEntity<DayResponse>> getDayByIdWithUser(Long planId, Long dayId, String userId);

    Flux<CustomEntityModel<MealResponse>> getMealsByDayInternal(Long planId, Long dayId, String userId);
}

package com.mocicarazvan.orderservice.services;

import com.mocicarazvan.orderservice.dtos.*;
import com.mocicarazvan.orderservice.dtos.summaries.CountryOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.DailyOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.MonthlyOrderSummary;
import com.mocicarazvan.orderservice.enums.CountrySummaryType;
import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.services.CountInParentService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface OrderService extends CountInParentService {

    Mono<SessionResponse> checkoutHosted(CheckoutRequestBody checkoutRequestBody, String userId);

    Mono<String> handleWebhook(String payload, String sigHeader);

    Mono<InvoicesResponse> getInvoices(String userId, PageableBody pageableBody);

    Flux<UserSubscriptionDto> getSubscriptions(String userId);

    Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getPlansForUser(String title, DietType dietType, PageableBody pageableBody, String userId);

    Mono<ResponseWithUserDtoEntity<RecipeResponse>> getRecipeByPlanForUser(Long id, Long recipeId, String userId);

    Mono<ResponseWithUserDtoEntity<PlanResponse>> getPlanByIdForUser(Long id, String userId);

    Mono<CustomInvoiceDto> getInvoiceById(String id, String userId);

    Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getPlansByOrder(Long orderId, String userId);

    Flux<MonthlyEntityGroup<OrderDto>> getOrdersGroupedByMonth(int month, String userId);

    Flux<MonthlyOrderSummary> getOrdersSummaryByMonth(LocalDate from, LocalDate to, String userId);

    Flux<MonthlyOrderSummary> getTrainerOrdersSummaryByMonth(LocalDate from, LocalDate to, Long trainerId, String userId);

    Flux<DailyOrderSummary> getOrdersSummaryByDay(LocalDate from, LocalDate to, String userId);

    Flux<DailyOrderSummary> getTrainerOrdersSummaryByDay(LocalDate from, LocalDate to, Long trainerId, String userId);

    Flux<CountryOrderSummary> getOrdersSummaryByCountry(CountrySummaryType type);

}

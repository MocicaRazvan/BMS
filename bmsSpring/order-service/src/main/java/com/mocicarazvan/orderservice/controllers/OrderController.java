package com.mocicarazvan.orderservice.controllers;

import com.mocicarazvan.orderservice.dtos.*;
import com.mocicarazvan.orderservice.dtos.clients.DayResponse;
import com.mocicarazvan.orderservice.dtos.clients.MealResponse;
import com.mocicarazvan.orderservice.dtos.clients.PlanResponse;
import com.mocicarazvan.orderservice.dtos.clients.RecipeResponse;
import com.mocicarazvan.orderservice.dtos.clients.collect.FullDayResponse;
import com.mocicarazvan.orderservice.dtos.summaries.CountryOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.DailyOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.MonthlyOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.TopUsersSummary;
import com.mocicarazvan.orderservice.enums.CountrySummaryType;
import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.orderservice.enums.ObjectiveType;
import com.mocicarazvan.orderservice.services.OrderService;
import com.mocicarazvan.templatemodule.controllers.CountInParentController;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController implements CountInParentController {

    private final OrderService orderService;

    private final RequestsUtils requestsUtils;


    @PostMapping(value = "/checkout/hosted", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<SessionResponse>> checkoutHosted(@Valid @RequestBody CheckoutRequestBody checkoutRequestBody, ServerWebExchange exchange) {
        return orderService.checkoutHosted(checkoutRequestBody, requestsUtils.extractAuthUser(exchange)).map(ResponseEntity::ok);
    }

    @PostMapping(value = "/webhook", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<String>> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        return orderService.handleWebhook(payload, sigHeader).map(ResponseEntity::ok);
    }

    @Override
    @GetMapping(value = "/internal/count/{childId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<EntityCount>> getCountInParent(@PathVariable Long childId) {
        return orderService.countInParent(childId).map(ResponseEntity::ok);
    }

    @PatchMapping(value = "/invoices/list", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<InvoicesResponse>> getInvoices(@RequestBody @Valid PageableBody pageableBody, ServerWebExchange exchange) {
        return orderService.getInvoices(requestsUtils.extractAuthUser(exchange), pageableBody)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/invoices/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomInvoiceDto>> getInvoiceById(@PathVariable String id, ServerWebExchange exchange) {
        return orderService.getInvoiceById(id, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/subscriptions", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<UserSubscriptionDto> getSubscriptions(ServerWebExchange exchange) {
        return orderService.getSubscriptions(requestsUtils.extractAuthUser(exchange));
    }

    @PatchMapping(value = "/subscriptions/filtered/withUser", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getAllPlansFilteredWithUserByIds(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DietType type,
            @RequestParam(required = false) ObjectiveType objective,
            @Valid @RequestBody PageableBody pageableBody,
            ServerWebExchange exchange
    ) {
        return orderService.getPlansForUser(title, type, objective, pageableBody, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping(value = "/subscriptionsByOrder/{orderId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>> getAllPlansByOrder(
            @PathVariable Long orderId,
            ServerWebExchange exchange
    ) {
        return orderService.getPlansByOrder(orderId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping(value = "/subscriptions/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ResponseWithUserDtoEntity<PlanResponse>>> getPlanByIdForUser(@PathVariable Long id, ServerWebExchange exchange) {
        return orderService.getPlanByIdForUser(id, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }


    @GetMapping(value = "/subscriptions/recipe/{planId}/{dayId}/{recipeId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    Mono<ResponseEntity<ResponseWithUserDtoEntity<RecipeResponse>>> getRecipeByIdWithUserInternal(@PathVariable Long planId, @PathVariable Long dayId,
                                                                                                  @PathVariable Long recipeId, ServerWebExchange exchange) {
        return orderService.getRecipeByIdWithUser(planId, dayId, recipeId, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/subscriptions/days/{planId}/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    Mono<ResponseEntity<ResponseWithUserDtoEntity<DayResponse>>> getDayByIdWithUserInternal(@PathVariable Long planId, @PathVariable Long dayId,
                                                                                            ServerWebExchange exchange) {
        return orderService.getDayByIdWithUser(planId, dayId, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/subscriptions/days/meals/{planId}/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    Flux<CustomEntityModel<MealResponse>> getMealsByDayInternal(@PathVariable Long planId, @PathVariable Long dayId,
                                                                ServerWebExchange exchange) {
        return orderService.getMealsByDayInternal(planId, dayId, requestsUtils.extractAuthUser(exchange));
    }

    // id=planId
    @GetMapping(value = "/subscriptions/days/full/{id}/{dayId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<FullDayResponse>> getRecipeByPlanInternal(@PathVariable Long id,
                                                                         @PathVariable Long dayId,
                                                                         ServerWebExchange exchange) {
        return orderService.getDayByPlanForUser(id, dayId, requestsUtils.extractAuthUser(exchange))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/admin/groupedByMonth")
    @ResponseStatus(HttpStatus.OK)
    Flux<MonthlyEntityGroup<OrderDto>> getOrdersGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return orderService.getOrdersGroupedByMonth(month, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/countAndAmount")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummary> getOrdersSummaryByMonth(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                             @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                             ServerWebExchange exchange) {
        return orderService.getOrdersSummaryByMonth(from, to, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/topUsers")
    @ResponseStatus(HttpStatus.OK)
    public Flux<TopUsersSummary> getTopUsersSummary(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                    @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                    @RequestParam @Valid @Min(1) int top
    ) {
        return orderService.getTopUsersSummary(from, to, top);
    }

    @GetMapping("/trainer/countAndAmount/{trainerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummary> getTrainerOrdersSummaryByMonth(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                                    @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                                    @PathVariable Long trainerId,
                                                                    ServerWebExchange exchange) {
        return orderService.getTrainerOrdersSummaryByMonth(from, to, trainerId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/countAndAmount/daily")
    @ResponseStatus(HttpStatus.OK)
    public Flux<DailyOrderSummary> getOrdersSummaryByDay(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                         @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                         ServerWebExchange exchange) {
        return orderService.getOrdersSummaryByDay(from, to, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/trainer/countAndAmount/daily/{trainerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<DailyOrderSummary> getTrainerOrdersSummaryByDay(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                                @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                                @PathVariable Long trainerId,
                                                                ServerWebExchange exchange) {
        return orderService.getTrainerOrdersSummaryByDay(from, to, trainerId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/summaryByCountry")
    @ResponseStatus(HttpStatus.OK)
    public Flux<CountryOrderSummary> getOrdersSummaryByCountry(@RequestParam CountrySummaryType type) {
        return orderService.getOrdersSummaryByCountry(type);
    }
}

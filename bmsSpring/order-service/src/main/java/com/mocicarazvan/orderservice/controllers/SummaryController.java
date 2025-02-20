package com.mocicarazvan.orderservice.controllers;

import com.mocicarazvan.orderservice.dtos.OrderDto;
import com.mocicarazvan.orderservice.dtos.summaries.*;
import com.mocicarazvan.orderservice.enums.CountrySummaryType;
import com.mocicarazvan.orderservice.services.SummaryService;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;
    private final RequestsUtils requestsUtils;

    @GetMapping("/admin/groupedByMonth")
    @ResponseStatus(HttpStatus.OK)
    Flux<MonthlyEntityGroup<OrderDto>> getOrdersGroupedByMonth(@RequestParam int month, ServerWebExchange exchange) {
        return summaryService.getOrdersGroupedByMonth(month, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/countAndAmount")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummary> getOrdersSummaryByMonth(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                             @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                             ServerWebExchange exchange) {
        return summaryService.getOrdersSummaryByMonth(from, to, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/countAndAmount/prediction")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummaryPrediction> getOrdersSummaryByMonthPrediction(
            @RequestParam(required = false, defaultValue = "3") @Valid @Min(1) @Max(12) int predictionLength) {
        return summaryService.getOrderSummaryPrediction(predictionLength);
    }

    @GetMapping("/admin/plans/countAndAmount/prediction")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummaryPrediction> getPlansSummaryByMonthPrediction(
            @RequestParam(required = false, defaultValue = "3") @Valid @Min(1) @Max(12) int predictionLength) {
        return summaryService.getPlanSummaryPrediction(predictionLength);
    }

    @GetMapping("/trainer/countAndAmount/prediction/{trainerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummaryPrediction> getTrainerOrdersSummaryByMontPrediction(
            @PathVariable Long trainerId,
            @RequestParam(required = false, defaultValue = "3") @Valid @Min(1) @Max(12) int predictionLength,
            ServerWebExchange exchange
    ) {
        return summaryService.getTrainerPlanSummaryPrediction(predictionLength, trainerId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/plans/countAndAmount")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummary> getOrdersPlansSummaryByMonth(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                                  @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                                  ServerWebExchange exchange) {
        return summaryService.getOrdersPlanSummaryByMonth(from, to, requestsUtils.extractAuthUser(exchange));
    }


    @GetMapping("/admin/topUsers")
    @ResponseStatus(HttpStatus.OK)
    public Flux<TopUsersSummary> getTopUsersSummary(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                    @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                    @RequestParam @Valid @Min(1) int top
    ) {
        return summaryService.getTopUsersSummary(from, to, top);
    }

    @GetMapping("/admin/topTrainers")
    @ResponseStatus(HttpStatus.OK)
    public Flux<TopTrainersSummaryResponse> getTopTrainersSummary(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                                  @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                                  @RequestParam @Valid @Min(1) int top
    ) {
        return summaryService.getTopTrainersSummary(from, to, top);
    }

    @GetMapping("/admin/topPlans")
    @ResponseStatus(HttpStatus.OK)
    public Flux<TopPlansSummary> getTopPlansSummary(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                    @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                    @RequestParam @Valid @Min(1) int top
    ) {
        return summaryService.getTopPlansSummary(from, to, top);
    }

    @GetMapping("/trainer/topPlans/{trainerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<TopPlansSummary> getTopPlansTrainerSummary(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                           @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                           @PathVariable Long trainerId,
                                                           @RequestParam @Valid @Min(1) int top,
                                                           ServerWebExchange exchange

    ) {
        return summaryService.getTopPlansSummaryTrainer(from, to, top, trainerId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/trainer/countAndAmount/{trainerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummary> getTrainerOrdersSummaryByMonth(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                                    @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                                    @PathVariable Long trainerId,
                                                                    ServerWebExchange exchange) {
        return summaryService.getTrainerOrdersSummaryByMonth(from, to, trainerId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/trainer/countAndAmount/type/{trainerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummaryType> getTrainerOrdersSummaryByDateRangeGroupedByMonthTypes(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate month,
                                                                                               @PathVariable Long trainerId,
                                                                                               ServerWebExchange exchange
    ) {
        return summaryService.getTrainerOrdersSummaryByDateRangeGroupedByMonthTypes(month, trainerId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/trainer/countAndAmount/objective/{trainerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummaryObjective> getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectives(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate month,
                                                                                                         @PathVariable Long trainerId,
                                                                                                         ServerWebExchange exchange
    ) {
        return summaryService.getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectives(month, trainerId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/trainer/countAndAmount/objectiveType/{trainerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummaryObjectiveType> getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectivesTypes(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate month,
                                                                                                                  @PathVariable Long trainerId,
                                                                                                                  ServerWebExchange exchange
    ) {
        return summaryService.getTrainOrdersSummaryByDateRangeGroupedByMonthObjectiveTypes(month, trainerId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/countAndAmount/type")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummaryType> getAdminOrdersSummaryByDateRangeGroupedByMonthTypes(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate month,
                                                                                             ServerWebExchange exchange
    ) {
        return summaryService.getAdminOrdersSummaryByDateRangeGroupedByMonthTypes(month, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/countAndAmount/objective")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummaryObjective> getAdminOrdersSummaryByDateRangeGroupedByMonthObjectives(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate month,
                                                                                                       ServerWebExchange exchange
    ) {
        return summaryService.getAdminOrdersSummaryByDateRangeGroupedByMonthObjectives(month, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/countAndAmount/objectiveType")
    @ResponseStatus(HttpStatus.OK)
    public Flux<MonthlyOrderSummaryObjectiveType> getAdminOrdersSummaryByDateRangeGroupedByMonthObjectivesTypes(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate month,
                                                                                                                ServerWebExchange exchange
    ) {
        return summaryService.getAdminOrdersSummaryByDateRangeGroupedByMonthObjectiveTypes(month, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/countAndAmount/daily")
    @ResponseStatus(HttpStatus.OK)
    public Flux<DailyOrderSummary> getOrdersSummaryByDay(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                         @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                         ServerWebExchange exchange) {
        return summaryService.getOrdersSummaryByDay(from, to, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/plans/countAndAmount/daily")
    @ResponseStatus(HttpStatus.OK)
    public Flux<DailyOrderSummary> getOrdersPlanSummaryByDay(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                             @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                             ServerWebExchange exchange) {
        return summaryService.getOrdersPlanSummaryByDay(from, to, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/trainer/countAndAmount/daily/{trainerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<DailyOrderSummary> getTrainerOrdersSummaryByDay(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                                @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
                                                                @PathVariable Long trainerId,
                                                                ServerWebExchange exchange) {
        return summaryService.getTrainerOrdersSummaryByDay(from, to, trainerId, requestsUtils.extractAuthUser(exchange));
    }

    @GetMapping("/admin/summaryByCountry")
    @ResponseStatus(HttpStatus.OK)
    public Flux<CountryOrderSummary> getOrdersSummaryByCountry(@RequestParam CountrySummaryType type,
                                                               @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
                                                               @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to
    ) {
        return summaryService.getOrdersSummaryByCountry(type, from, to);
    }

}

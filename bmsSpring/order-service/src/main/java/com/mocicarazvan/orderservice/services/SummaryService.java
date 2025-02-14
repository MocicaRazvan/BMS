package com.mocicarazvan.orderservice.services;

import com.mocicarazvan.orderservice.dtos.OrderDto;
import com.mocicarazvan.orderservice.dtos.summaries.*;
import com.mocicarazvan.orderservice.enums.CountrySummaryType;
import com.mocicarazvan.templatemodule.dtos.response.MonthlyEntityGroup;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface SummaryService {
    Flux<MonthlyEntityGroup<OrderDto>> getOrdersGroupedByMonth(int month, String userId);

    Flux<MonthlyOrderSummary> getOrdersSummaryByMonth(LocalDate from, LocalDate to, String userId);

    Flux<MonthlyOrderSummary> getOrdersPlanSummaryByMonth(LocalDate from, LocalDate to, String userId);

    Flux<TopUsersSummary> getTopUsersSummary(LocalDate from, LocalDate to, int top);

    Flux<TopTrainersSummaryResponse> getTopTrainersSummary(LocalDate from, LocalDate to, int top);

    Flux<TopPlansSummary> getTopPlansSummary(LocalDate from, LocalDate to, int top);

    Flux<TopPlansSummary> getTopPlansSummaryTrainer(LocalDate from, LocalDate to, int top, Long trainerId, String userId);

    Flux<MonthlyOrderSummary> getTrainerOrdersSummaryByMonth(LocalDate from, LocalDate to, Long trainerId, String userId);

    Flux<DailyOrderSummary> getOrdersSummaryByDay(LocalDate from, LocalDate to, String userId);

    Flux<DailyOrderSummary> getOrdersPlanSummaryByDay(LocalDate from, LocalDate to, String userId);

    Flux<DailyOrderSummary> getTrainerOrdersSummaryByDay(LocalDate from, LocalDate to, Long trainerId, String userId);

    Flux<CountryOrderSummary> getOrdersSummaryByCountry(CountrySummaryType type, LocalDate from, LocalDate to);

    Flux<MonthlyOrderSummaryObjective> getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectives(LocalDate month, Long trainerId, String userId);

    Flux<MonthlyOrderSummaryType> getTrainerOrdersSummaryByDateRangeGroupedByMonthTypes(LocalDate month, Long trainerId, String userId);

    Flux<MonthlyOrderSummaryObjectiveType> getTrainOrdersSummaryByDateRangeGroupedByMonthObjectiveTypes(LocalDate month, Long trainerId, String userId);

    Flux<MonthlyOrderSummaryObjective> getAdminOrdersSummaryByDateRangeGroupedByMonthObjectives(LocalDate month, String userId);

    Flux<MonthlyOrderSummaryType> getAdminOrdersSummaryByDateRangeGroupedByMonthTypes(LocalDate month, String userId);

    Flux<MonthlyOrderSummaryObjectiveType> getAdminOrdersSummaryByDateRangeGroupedByMonthObjectiveTypes(LocalDate month, String userId);

}

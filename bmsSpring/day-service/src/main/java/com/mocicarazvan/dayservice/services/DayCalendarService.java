package com.mocicarazvan.dayservice.services;

import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarBody;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarResponse;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarTrackingStats;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarUserDates;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithChildList;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface DayCalendarService {
    Mono<DayCalendarResponse<MealResponse>> createDayCalendar(DayCalendarBody dayCalendarBody, String userId);

    Mono<Boolean> deleteDayCalendar(Long id, String userId);

    Mono<DayCalendarResponse<MealResponse>> getDayCalendarById(Long id, String userId);

    Flux<DayCalendarTrackingStats> getDayCalendarTrackingStats(String userId, LocalDate from, LocalDate to);

    Flux<DayCalendarResponse<MealResponse>> getDayCalendarsByUserIdAndDateBetween(
            String userId,
            LocalDate startDate,
            LocalDate endDate
    );

    Flux<DayCalendarResponse<ResponseWithChildList<MealResponse, RecipeResponse>>> getFullDayCalendarsByUserIdAndDateBetween(
            String userId,
            LocalDate startDate,
            LocalDate endDate
    );

    Flux<DayCalendarUserDates> getAllDaysByUserId(String userId);
}

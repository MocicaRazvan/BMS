package com.mocicarazvan.dayservice.hateos.dayCalendar;

import com.mocicarazvan.dayservice.controllers.DayCalendarController;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarResponse;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithChildList;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveResponseBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class DayCalendarReactiveResponseBuilder {

    private final DayCalendarReactiveLinkBuilderMR dayCalendarReactiveLinkBuilderMR;
    private final DayCalendarReactiveLinkBuilderRC dayCalendarReactiveLinkBuilderRC;

    @Component
    public static class DayCalendarReactiveLinkBuilderMR extends ReactiveResponseBuilder<DayCalendarResponse<MealResponse>, DayCalendarController> {
        public DayCalendarReactiveLinkBuilderMR() {
            super(new DayCalendarReactiveLinkBuilder<>());
        }
    }

    @Component
    public static class DayCalendarReactiveLinkBuilderRC extends ReactiveResponseBuilder<DayCalendarResponse<ResponseWithChildList<MealResponse, RecipeResponse>>, DayCalendarController> {
        public DayCalendarReactiveLinkBuilderRC() {
            super(new DayCalendarReactiveLinkBuilder<>());
        }
    }
}

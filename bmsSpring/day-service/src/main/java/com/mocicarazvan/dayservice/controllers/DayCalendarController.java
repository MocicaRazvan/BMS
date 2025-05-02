package com.mocicarazvan.dayservice.controllers;

import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarBody;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarResponse;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarTrackingStats;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarUserDates;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.dayservice.hateos.dayCalendar.DayCalendarReactiveResponseBuilder;
import com.mocicarazvan.dayservice.services.DayCalendarService;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithChildList;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import jakarta.validation.Valid;
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
@RequestMapping("/daysCalendar")
@RequiredArgsConstructor
public class DayCalendarController {

    private final DayCalendarService dayCalendarService;
    private final RequestsUtils requestsUtils;
    private final DayCalendarReactiveResponseBuilder dayCalendarReactiveResponseBuilder;

    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<DayCalendarResponse<MealResponse>>>> createDayCalendar(
            @Valid @RequestBody DayCalendarBody body, ServerWebExchange serverWebExchange
    ) {
        return dayCalendarService.createDayCalendar(body,
                        requestsUtils.extractAuthUser(serverWebExchange)
                )
                .flatMap(dr -> dayCalendarReactiveResponseBuilder
                        .getDayCalendarReactiveLinkBuilderMR()
                        .toModel(dr, DayCalendarController.class))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/byRange", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<CustomEntityModel<DayCalendarResponse<MealResponse>>> getDaysCalendarByRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            ServerWebExchange serverWebExchange
    ) {
        return dayCalendarService.getDayCalendarsByUserIdAndDateBetween(
                requestsUtils.extractAuthUser(serverWebExchange),
                start,
                end
        ).flatMap(dr -> dayCalendarReactiveResponseBuilder
                .getDayCalendarReactiveLinkBuilderMR()
                .toModel(dr, DayCalendarController.class));
    }

    @GetMapping(value = "/fullByRange", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<CustomEntityModel<DayCalendarResponse<ResponseWithChildList<MealResponse, RecipeResponse>>>>
    getFullDaysCalendarByRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            ServerWebExchange serverWebExchange
    ) {
        return dayCalendarService.getFullDayCalendarsByUserIdAndDateBetween(
                requestsUtils.extractAuthUser(serverWebExchange),
                start,
                end
        ).flatMap(dr -> dayCalendarReactiveResponseBuilder
                .getDayCalendarReactiveLinkBuilderRC()
                .toModel(dr, DayCalendarController.class));
    }

    @DeleteMapping(value = "/delete/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Boolean>> deleteDayCalendar(
            @PathVariable Long id,
            ServerWebExchange serverWebExchange
    ) {
        return dayCalendarService.deleteDayCalendar(
                        id,
                        requestsUtils.extractAuthUser(serverWebExchange)
                )
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/byId/{id}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<CustomEntityModel<DayCalendarResponse<MealResponse>>>> getDayCalendarById(
            @PathVariable Long id,
            ServerWebExchange serverWebExchange
    ) {
        return dayCalendarService.getDayCalendarById(
                        id,
                        requestsUtils.extractAuthUser(serverWebExchange)
                )
                .flatMap(dr -> dayCalendarReactiveResponseBuilder
                        .getDayCalendarReactiveLinkBuilderMR()
                        .toModel(dr, DayCalendarController.class))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/userDates", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<DayCalendarUserDates> getUserDates(ServerWebExchange serverWebExchange) {
        return dayCalendarService.getAllDaysByUserId(
                requestsUtils.extractAuthUser(serverWebExchange)
        );
    }

    @GetMapping(value = "/trackingStats", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Flux<DayCalendarTrackingStats> getTrackingStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate to,
            ServerWebExchange serverWebExchange
    ) {
        return dayCalendarService.getDayCalendarTrackingStats(
                requestsUtils.extractAuthUser(serverWebExchange),
                from,
                to
        );
    }
}

package com.mocicarazvan.dayservice.hateos.dayCalendar;

import com.mocicarazvan.dayservice.controllers.DayCalendarController;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarBody;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarResponse;
import com.mocicarazvan.templatemodule.hateos.controller.ReactiveLinkBuilder;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DayCalendarReactiveLinkBuilder<T> implements ReactiveLinkBuilder<DayCalendarResponse<T>, DayCalendarController> {
    @Override
    public List<WebFluxLinkBuilder.WebFluxLink> createModelLinks(DayCalendarResponse<T> dayCalendarResponse, Class<DayCalendarController> c) {
        List<WebFluxLinkBuilder.WebFluxLink> links = new ArrayList<>();
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).createDayCalendar(
                DayCalendarBody.builder()
                        .dayId(1L)
                        .date(LocalDate.now())
                        .build(),
                null
        )).withRel("create"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getDayCalendarById(
                dayCalendarResponse.getId(),
                null
        )).withRel("get"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).deleteDayCalendar(
                dayCalendarResponse.getId(),
                null
        )).withRel("delete"));

        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getDaysCalendarByRange(
                        LocalDate.now(),
                        LocalDate.now().minusDays(6),
                        null
                )
        ).withRel("getByRange"));
        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getFullDaysCalendarByRange(
                        LocalDate.now(),
                        LocalDate.now().minusDays(6),
                        null
                )
        ).withRel("getFullByRange"));

        links.add(WebFluxLinkBuilder.linkTo(WebFluxLinkBuilder.methodOn(c).getUserDates(null))
                .withRel("getUserDates")
        );

        return links;
    }
}

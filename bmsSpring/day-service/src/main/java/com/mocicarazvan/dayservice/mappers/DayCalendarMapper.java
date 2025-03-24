package com.mocicarazvan.dayservice.mappers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarBody;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarDbDto;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarResponse;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarSimpleResponse;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.dayservice.models.DayCalendar;
import com.mocicarazvan.dayservice.models.Meal;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class DayCalendarMapper extends DtoMapper<DayCalendar, DayCalendarBody, DayCalendarResponse> {
    private final DayMapper dayMapper;
    private final MealMapper mealMapper;
    private final ObjectMapper objectMapper;

    public DayCalendarMapper(DayMapper dayMapper, MealMapper mealMapper, ObjectMapper objectMapper) {
        this.dayMapper = dayMapper;
        this.mealMapper = mealMapper;
        this.objectMapper = objectMapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public DayCalendarResponse<MealResponse> fromModelToResponse(DayCalendar dayCalendar) {
        return DayCalendarResponse.<MealResponse>builder()
                .date(dayCalendar.getDate())
                .userId(dayCalendar.getUserId())
                .id(dayCalendar.getId())
                .createdAt(dayCalendar.getCreatedAt())
                .updatedAt(dayCalendar.getUpdatedAt())
                .build();
    }

    public DayCalendarResponse<MealResponse> fromDbDtoToResponse(DayCalendarDbDto dayCalendarDbDto) {
        try {
            return DayCalendarResponse.<MealResponse>builder()
                    .date(dayCalendarDbDto.getDate())
                    .userId(dayCalendarDbDto.getUserId())
                    .id(dayCalendarDbDto.getId())
                    .createdAt(dayCalendarDbDto.getCreatedAt())
                    .updatedAt(dayCalendarDbDto.getUpdatedAt())
                    .dayResponse(dayMapper.fromModelToResponse(
                            objectMapper
                                    .readValue(dayCalendarDbDto.getDay(), Day.class)
                    ))
                    .mealResponses(
                            objectMapper.readValue(
                                            dayCalendarDbDto.getMeals(),
                                            new TypeReference<List<Meal>>() {
                                            }
                                    )
                                    .stream()
                                    .map(mealMapper::fromModelToResponse)
                                    .toList()
                    )
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse JSON fields", e);
        }
    }


    @Override
    public DayCalendar fromBodyToModel(DayCalendarBody dayCalendarBody) {
        return DayCalendar.builder()
                .dayId(dayCalendarBody.getDayId())
                .date(dayCalendarBody.getDate())
                .build();
    }

    @Override
    public Mono<DayCalendar> updateModelFromBody(DayCalendarBody dayCalendarBody, DayCalendar dayCalendar) {
        dayCalendar.setDate(dayCalendarBody.getDate());
        dayCalendar.setDayId(dayCalendarBody.getDayId());
        return Mono.just(dayCalendar);
    }

    public DayCalendarSimpleResponse fromModelToSimpleResponse(DayCalendar dayCalendar) {
        return DayCalendarSimpleResponse.builder()
                .dayId(dayCalendar.getDayId())
                .date(dayCalendar.getDate())
                .userId(dayCalendar.getUserId())
                .id(dayCalendar.getId())
                .createdAt(dayCalendar.getCreatedAt())
                .updatedAt(dayCalendar.getUpdatedAt())
                .build();
    }
}

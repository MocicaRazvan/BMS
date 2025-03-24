package com.mocicarazvan.dayservice.mappers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mocicarazvan.dayservice.dtos.day.DayBody;
import com.mocicarazvan.dayservice.dtos.day.DayResponse;
import com.mocicarazvan.dayservice.dtos.day.DayResponseWithMeals;
import com.mocicarazvan.dayservice.dtos.day.DayWithMealsDb;
import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;

@Component

public class DayMapper extends DtoMapper<Day, DayBody, DayResponse> {
    private final ObjectMapper objectMapper;

    public DayMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Override
    public DayResponse fromModelToResponse(Day day) {
        return DayResponse.builder()
                .type(day.getType())
                .body(day.getBody())
                .title(day.getTitle())
                .userDislikes(day.getUserDislikes())
                .userLikes(day.getUserLikes())
                .userId(day.getUserId())
                .id(day.getId())
                .createdAt(day.getCreatedAt())
                .updatedAt(day.getUpdatedAt())
                .build();
    }

    @Override
    public Day fromBodyToModel(DayBody dayBody) {
        return Day.builder()
                .type(dayBody.getType())
                .title(dayBody.getTitle())
                .body(dayBody.getBody())
                .build();
    }

    @Override
    public Mono<Day> updateModelFromBody(DayBody dayBody, Day day) {
        day.setType(dayBody.getType());
        day.setTitle(dayBody.getTitle());
        day.setBody(dayBody.getBody());
        day.setUpdatedAt(LocalDateTime.now());
        return Mono.just(day);
    }

    public DayResponseWithMeals fromDbWithMealsToResponseWithMeals(DayWithMealsDb dayWithMealsDb) {
        try {

            return DayResponseWithMeals.builder()
                    .type(dayWithMealsDb.getType())
                    .body(dayWithMealsDb.getBody())
                    .title(dayWithMealsDb.getTitle())
                    .userDislikes(dayWithMealsDb.getUserDislikes())
                    .userLikes(dayWithMealsDb.getUserLikes())
                    .userId(dayWithMealsDb.getUserId())
                    .id(dayWithMealsDb.getId())
                    .createdAt(dayWithMealsDb.getCreatedAt())
                    .updatedAt(dayWithMealsDb.getUpdatedAt())
                    .mealResponses(
                            objectMapper.readValue(dayWithMealsDb.getMeals(),
                                    new TypeReference<>() {
                                    })
                    )
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse JSON fields", e);
        }
    }

    public Day fromRowToModel(Row row) {
        return Day.builder()
                .type(DayType.valueOf(Objects.requireNonNull(row.get("type", String.class)).toUpperCase()))
                .title(row.get("title", String.class))
                .body(row.get("body", String.class))
                .userLikes(EntitiesUtils.convertArrayToList(row.get("user_likes", Long[].class)))
                .userDislikes(EntitiesUtils.convertArrayToList(row.get("user_dislikes", Long[].class)))
                .userId(row.get("user_id", Long.class))
                .id(row.get("id", Long.class))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .updatedAt(row.get("updated_at", LocalDateTime.class))
                .build();
    }
}

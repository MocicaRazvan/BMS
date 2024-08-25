package com.mocicarazvan.dayservice.mappers;

import com.mocicarazvan.dayservice.dtos.meal.MealBody;
import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
import com.mocicarazvan.dayservice.models.Meal;
import com.mocicarazvan.templatemodule.mappers.DtoMapper;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class MealMapper extends DtoMapper<Meal, MealBody, MealResponse> {
    @Override
    public MealResponse fromModelToResponse(Meal meal) {
        return MealResponse.builder()
                .recipes(meal.getRecipes())
                .dayId(meal.getDayId())
                .period(meal.getPeriod())
                .userId(meal.getUserId())
                .userId(meal.getUserId())
                .id(meal.getId())
                .createdAt(meal.getCreatedAt())
                .updatedAt(meal.getUpdatedAt())
                .build();
    }

    @Override
    public Meal fromBodyToModel(MealBody mealBody) {
        return Meal.builder()
                .recipes(mealBody.getRecipes())
                .dayId(mealBody.getDayId())
                .period(mealBody.getPeriod())
                .build();
    }

    @Override
    public Mono<Meal> updateModelFromBody(MealBody mealBody, Meal meal) {
        meal.setRecipes(mealBody.getRecipes());
        meal.setDayId(mealBody.getDayId());
        meal.setPeriod(mealBody.getPeriod());
        meal.setUpdatedAt(LocalDateTime.now());
        return Mono.just(meal);
    }

    public Meal fromRowToModel(Row row) {
        return Meal.builder()
                .recipes(EntitiesUtils.convertArrayToList(row.get("m_recipes", Long[].class)))
                .dayId(row.get("m_day_id", Long.class))
                .period(row.get("m_period", String.class))
                .userId(row.get("m_user_id", Long.class))
                .id(row.get("m_id", Long.class))
                .createdAt(row.get("m_created_at", LocalDateTime.class))
                .updatedAt(row.get("m_updated_at", LocalDateTime.class))
                .build();
    }
}

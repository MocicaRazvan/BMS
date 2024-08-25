package com.mocicarazvan.planservice.dtos.dayClient.collect;

import com.mocicarazvan.planservice.dtos.dayClient.MealResponse;
import com.mocicarazvan.planservice.dtos.dayClient.RecipeResponse;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class FullMealResponse extends WithUserDto {
    private List<RecipeResponse> recipes;
    private Long dayId;
    private String period;

    public static FullMealResponse fromMealResponse(MealResponse mealResponse) {
        return FullMealResponse.builder()
                .dayId(mealResponse.getDayId())
                .period(mealResponse.getPeriod())
                .userId(mealResponse.getUserId())
                .id(mealResponse.getId())
                .createdAt(mealResponse.getCreatedAt())
                .updatedAt(mealResponse.getUpdatedAt())
                .build();
    }
}

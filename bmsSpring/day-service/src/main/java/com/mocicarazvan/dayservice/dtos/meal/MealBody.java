package com.mocicarazvan.dayservice.dtos.meal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class MealBody extends ComposeMealBody {


    @NotNull(message = "The dayId should not be null.")
    private Long dayId;

    public static MealBody fromCompose(ComposeMealBody composeMealBody, Long dayId) {
        return MealBody.builder()
                .dayId(dayId)
                .recipes(composeMealBody.getRecipes())
                .period(composeMealBody.getPeriod())
                .build();
    }

}

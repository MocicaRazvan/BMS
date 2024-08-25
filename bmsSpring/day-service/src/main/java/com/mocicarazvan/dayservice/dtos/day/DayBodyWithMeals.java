package com.mocicarazvan.dayservice.dtos.day;

import com.mocicarazvan.dayservice.dtos.meal.ComposeMealBody;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class DayBodyWithMeals extends DayBody {
    @NotNull(message = "The meals should not be null.")
    @NotEmpty(message = "The meals should not be empty.")
    private List<ComposeMealBody> meals;
}

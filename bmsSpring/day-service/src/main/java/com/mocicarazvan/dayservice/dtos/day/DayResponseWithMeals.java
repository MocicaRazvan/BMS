package com.mocicarazvan.dayservice.dtos.day;

import com.mocicarazvan.dayservice.dtos.meal.MealResponse;
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
public class DayResponseWithMeals extends DayResponse {
    List<MealResponse> mealResponses;
}

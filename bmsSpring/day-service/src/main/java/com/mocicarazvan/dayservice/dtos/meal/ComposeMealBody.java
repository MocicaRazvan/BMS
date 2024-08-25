package com.mocicarazvan.dayservice.dtos.meal;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class ComposeMealBody {
    @NotNull(message = "The recipes should not be null.")
    @NotEmpty(message = "The recipes should not be empty.")
    private List<Long> recipes;
    @NotNull(message = "The period should not be null.")
    private String period;
}

package com.mocicarazvan.orderservice.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CheckoutRequestBody {

    @NotNull(message = "Plans cannot be null")
    @NotEmpty(message = "Plans cannot be empty")
    private List<PlanResponse> plans;

    @NotNull(message = "Total cannot be null")
    @Positive(message = "Total must be positive")
    private double total;

    @NotNull(message = "Locale cannot be null")
    @NotEmpty(message = "Locale cannot be empty")
    private String locale;
}

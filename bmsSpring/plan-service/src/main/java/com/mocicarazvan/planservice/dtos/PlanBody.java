package com.mocicarazvan.planservice.dtos;

import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class PlanBody extends TitleBodyDto {

    @NotNull(message = "The type should not be null.")
    private DietType type;

    @NotNull(message = "The objective should not be null.")
    private ObjectiveType objective;

    @NotNull(message = "The price should not be null.")
    @Positive(message = "The price should be positive.")
    private double price;

    @NotEmpty(message = "The days should not be empty.")
    private List<Long> days;
}

package com.mocicarazvan.planservice.dtos;

import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
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
public class PlanResponse extends ApproveDto {
    private double price;
    private DietType type;
    private boolean display;
    private List<Long> days;
    private ObjectiveType objective;
}

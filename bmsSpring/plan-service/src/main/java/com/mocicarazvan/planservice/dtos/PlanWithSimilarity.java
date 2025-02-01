package com.mocicarazvan.planservice.dtos;

import com.mocicarazvan.planservice.models.Plan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlanWithSimilarity extends Plan {
    private Double similarity;
}

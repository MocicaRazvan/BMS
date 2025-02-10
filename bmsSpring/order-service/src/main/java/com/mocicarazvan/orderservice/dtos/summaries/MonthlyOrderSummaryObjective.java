package com.mocicarazvan.orderservice.dtos.summaries;

import com.mocicarazvan.orderservice.enums.ObjectiveType;
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
public class MonthlyOrderSummaryObjective extends MonthlyOrderSummary {
    private ObjectiveType objective;
    private double averageAmount;
}

package com.mocicarazvan.orderservice.dtos.summaries;

import com.mocicarazvan.orderservice.enums.DietType;
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
public class MonthlyOrderSummaryType extends MonthlyOrderSummary {
    private DietType type;
    private double averageAmount;

}

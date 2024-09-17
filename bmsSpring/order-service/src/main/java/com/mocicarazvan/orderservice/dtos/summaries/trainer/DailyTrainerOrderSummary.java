package com.mocicarazvan.orderservice.dtos.summaries.trainer;

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
public class DailyTrainerOrderSummary extends MonthlyTrainerOrderSummary {
    private int day;
}

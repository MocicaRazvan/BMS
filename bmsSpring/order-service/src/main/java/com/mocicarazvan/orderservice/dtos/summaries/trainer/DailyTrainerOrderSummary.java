package com.mocicarazvan.orderservice.dtos.summaries.trainer;

import com.mocicarazvan.orderservice.dtos.summaries.MonthlyOrderSummary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DailyTrainerOrderSummary extends MonthlyTrainerOrderSummary {
    private int day;
}

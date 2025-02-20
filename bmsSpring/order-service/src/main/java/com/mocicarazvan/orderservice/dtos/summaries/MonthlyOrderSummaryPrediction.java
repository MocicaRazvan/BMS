package com.mocicarazvan.orderservice.dtos.summaries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MonthlyOrderSummaryPrediction {
    private int year;
    private int month;
    private List<Double> countQuantiles;
    private List<Double> totalAmountQuantiles;
}

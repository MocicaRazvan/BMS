package com.mocicarazvan.orderservice.dtos.summaries.trainer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MonthlyTrainerOrderSummary {
    private int year;
    private int month;
    private long count;
    private List<Long> planIds;

}

package com.mocicarazvan.orderservice.dtos.summaries;

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
public class TopTrainerSummaryBase extends GroupSummary {
    private long userId;
    private double totalAmount;
    private long planCount;
    private double averageAmount;
    private double maxGroupPlanCount;
    private double minGroupPlanCount;
    private double avgGroupPlanCount;
}

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
public class TopPlansSummary extends RankSummary {
    private long planId;
    private long count;
    private double maxGroupCount;
    private double minGroupCount;
    private double avgGroupCount;
    private double ratio;
}

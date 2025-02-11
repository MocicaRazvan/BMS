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
public class TopTrainersSummaryR2dbc extends TopTrainerSummaryBase {
    private String typeCounts;
    private String objectiveCounts;
    private String typeAmounts;
    private String objectiveAmounts;
    private String typeAvgs;
    private String objectiveAvgs;
}

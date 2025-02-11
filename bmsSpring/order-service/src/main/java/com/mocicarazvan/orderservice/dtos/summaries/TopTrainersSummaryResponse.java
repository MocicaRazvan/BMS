package com.mocicarazvan.orderservice.dtos.summaries;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TopTrainersSummaryResponse extends TopTrainerSummaryBase {
    private Map<String, Integer> typeCounts;
    private Map<String, Integer> objectiveCounts;
    private Map<String, Double> typeAmounts;
    private Map<String, Double> objectiveAmounts;
    private Map<String, Double> typeAvgs;
    private Map<String, Double> objectiveAvgs;

    public static TopTrainersSummaryResponse fromR2dbc(TopTrainersSummaryR2dbc r2dbc, ObjectMapper objectMapper) {
        try {
            return TopTrainersSummaryResponse.builder()
                    .rank(r2dbc.getRank())
                    .maxGroupTotal(r2dbc.getMaxGroupTotal())
                    .minGroupTotal(r2dbc.getMinGroupTotal())
                    .avgGroupTotal(r2dbc.getAvgGroupTotal())
                    .userId(r2dbc.getUserId())
                    .totalAmount(r2dbc.getTotalAmount())
                    .planCount(r2dbc.getPlanCount())
                    .averageAmount(r2dbc.getAverageAmount())
                    .maxGroupPlanCount(r2dbc.getMaxGroupPlanCount())
                    .minGroupPlanCount(r2dbc.getMinGroupPlanCount())
                    .avgGroupPlanCount(r2dbc.getAvgGroupPlanCount())
                    .typeCounts(objectMapper.readValue(r2dbc.getTypeCounts(), new TypeReference<>() {
                    }))
                    .objectiveCounts(objectMapper.readValue(r2dbc.getObjectiveCounts(), new TypeReference<>() {
                    }))
                    .typeAmounts(objectMapper.readValue(r2dbc.getTypeAmounts(), new TypeReference<>() {
                    }))
                    .objectiveAmounts(objectMapper.readValue(r2dbc.getObjectiveAmounts(), new TypeReference<>() {
                    }))
                    .typeAvgs(objectMapper.readValue(r2dbc.getTypeAvgs(), new TypeReference<>() {
                    }))
                    .objectiveAvgs(objectMapper.readValue(r2dbc.getObjectiveAvgs(), new TypeReference<>() {
                    }))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse TopTrainersSummaryResponse from TopTrainersSummaryR2dbc",
                    e);
        }
    }
}

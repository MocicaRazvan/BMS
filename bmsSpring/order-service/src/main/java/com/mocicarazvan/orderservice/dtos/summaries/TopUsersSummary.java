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
public class TopUsersSummary extends GroupSummary {
    private long userId;
    private double totalAmount;
    private long ordersNumber;
    private long[] planValues;
    private int plansNumber;
}

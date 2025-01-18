package com.mocicarazvan.orderservice.dtos.summaries;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TopUsersSummary {
    private long userId;
    private double totalAmount;
    private long ordersNumber;
    private long[] planValues;
    private int rank;
    private int plansNumber;
    private double maxGroupTotal;
    private double minGroupTotal;
    private double avgGroupTotal;
}

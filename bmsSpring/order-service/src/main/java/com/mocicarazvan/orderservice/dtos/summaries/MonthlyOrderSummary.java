package com.mocicarazvan.orderservice.dtos.summaries;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MonthlyOrderSummary extends CountAmount {
    private int year;
    private int month;

    public MonthlyOrderSummary(int year, int month, long count, double totalAmount) {

        super(count, totalAmount);
        this.year = year;
        this.month = month;
    }
}

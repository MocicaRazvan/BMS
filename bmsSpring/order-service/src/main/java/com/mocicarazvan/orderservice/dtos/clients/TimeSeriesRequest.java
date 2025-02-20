package com.mocicarazvan.orderservice.dtos.clients;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TimeSeriesRequest {
    @Builder.Default
    private List<Long> count_list = new ArrayList<>();
    @Builder.Default
    private List<Double> total_amount_list = new ArrayList<>();
    @Builder.Default
    private int prediction_length = 3;

    public void addCount(Long count) {
        count_list.add(count);
    }

    public void addAmount(Double amount) {
        total_amount_list.add(amount);
    }
}

package com.mocicarazvan.orderservice.dtos.clients;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TimeSeriesResponse {
    private List<List<Double>> count_quantiles;
    private List<List<Double>> total_amount_quantiles;
}

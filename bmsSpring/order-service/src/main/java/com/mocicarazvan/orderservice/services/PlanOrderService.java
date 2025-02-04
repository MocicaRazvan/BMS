package com.mocicarazvan.orderservice.services;

import com.mocicarazvan.orderservice.dtos.clients.PlanResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PlanOrderService {
    Mono<Boolean> savePlansForOrder(List<PlanResponse> planResponses, Long orderId);

    Mono<Void> deleteAllByOrderId(Long orderId);
}

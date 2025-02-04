package com.mocicarazvan.orderservice.repositories;

import com.mocicarazvan.orderservice.models.PlanOrder;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface PlanOrderRepository extends R2dbcRepository<PlanOrder, Long> {
    Mono<Void> deleteAllByOrderId(Long orderId);
}

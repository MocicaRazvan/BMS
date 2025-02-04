package com.mocicarazvan.orderservice.services.impl;

import com.mocicarazvan.orderservice.dtos.clients.PlanResponse;
import com.mocicarazvan.orderservice.mappers.PlanOrderMapper;
import com.mocicarazvan.orderservice.models.PlanOrder;
import com.mocicarazvan.orderservice.repositories.PlanOrderRepository;
import com.mocicarazvan.orderservice.services.PlanOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PlanOrderServiceImpl implements PlanOrderService {
    private final PlanOrderRepository planOrderRepository;
    private final PlanOrderMapper planOrderMapper;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<Boolean> savePlansForOrder(List<PlanResponse> planResponses, Long orderId) {

        return Flux.fromIterable(planResponses)
                .map(p -> {
                    PlanOrder planOrder = planOrderMapper.fromPlanResponse(p);
                    planOrder.setOrderId(orderId);
                    return planOrder;
                }).as(planOrderRepository::saveAll)
                .as(transactionalOperator::transactional)
                .then(Mono.just(true));

    }

    @Override
    public Mono<Void> deleteAllByOrderId(Long orderId) {
        return planOrderRepository.deleteAllByOrderId(orderId);
    }
}

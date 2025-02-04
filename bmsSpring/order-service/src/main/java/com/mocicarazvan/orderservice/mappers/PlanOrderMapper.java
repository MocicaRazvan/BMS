package com.mocicarazvan.orderservice.mappers;


import com.mocicarazvan.orderservice.dtos.clients.PlanResponse;
import com.mocicarazvan.orderservice.models.PlanOrder;
import org.springframework.stereotype.Component;

@Component
public class PlanOrderMapper {

    public PlanOrder fromPlanResponse(PlanResponse planResponse) {
        return PlanOrder.builder()
                .price(planResponse.getPrice())
                .type(planResponse.getType())
                .objective(planResponse.getObjective())
                .planId(planResponse.getId())
                .planCreatedAt(planResponse.getCreatedAt())
                .planUpdatedAt(planResponse.getUpdatedAt())
                .title(planResponse.getTitle())
                .userId(planResponse.getUserId())
                .build();
    }
}

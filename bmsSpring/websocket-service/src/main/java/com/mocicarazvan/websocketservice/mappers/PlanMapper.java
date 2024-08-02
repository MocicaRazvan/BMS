package com.mocicarazvan.websocketservice.mappers;

import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.mappers.generic.ApproveModelMapper;
import com.mocicarazvan.websocketservice.models.Plan;
import org.springframework.stereotype.Component;

@Component
public class PlanMapper extends ApproveModelMapper<Plan, PlanResponse> {
    public PlanMapper(ConversationUserMapper conversationUserMapper) {
        super(conversationUserMapper);
    }

    @Override
    public PlanResponse fromModelToResponse(Plan plan) {
        return PlanResponse.builder()
                .approved(plan.isApproved())
                .receiver(conversationUserMapper.fromModelToResponse(plan.getReceiver()))
                .appId(plan.getAppId())
                .id(plan.getId())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}

package com.mocicarazvan.websocketservice.mappers;

import com.mocicarazvan.websocketservice.dtos.bought.BoughtNotificationBody;
import com.mocicarazvan.websocketservice.dtos.bought.BoughtNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.bought.InternalBoughtBody;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.enums.BoughtNotificationType;
import com.mocicarazvan.websocketservice.mappers.generic.NotificationTemplateMapper;
import com.mocicarazvan.websocketservice.models.BoughtNotification;
import com.mocicarazvan.websocketservice.models.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoughtNotificationMapper extends NotificationTemplateMapper<Plan, PlanResponse, BoughtNotificationType,
        BoughtNotification, BoughtNotificationResponse
        > {

    private final PlanMapper planMapper;
    private final ConversationUserMapper conversationUserMapper;


    @Override
    public BoughtNotificationResponse fromModelToResponse(BoughtNotification boughtNotification) {
        return BoughtNotificationResponse.builder()
                .id(boughtNotification.getId())
                .sender(conversationUserMapper.fromModelToResponse(boughtNotification.getSender()))
                .receiver(conversationUserMapper.fromModelToResponse(boughtNotification.getReceiver()))
                .type(boughtNotification.getType())
                .reference(planMapper.fromModelToResponse(boughtNotification.getReference()))
                .content(boughtNotification.getContent())
                .extraLink(boughtNotification.getExtraLink())
                .timestamp(boughtNotification.getTimestamp())
                .createdAt(boughtNotification.getCreatedAt())
                .updatedAt(boughtNotification.getUpdatedAt())
                .build();
    }


}

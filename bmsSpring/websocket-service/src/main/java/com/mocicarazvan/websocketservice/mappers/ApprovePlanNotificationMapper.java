package com.mocicarazvan.websocketservice.mappers;


import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.mappers.generic.NotificationTemplateMapper;
import com.mocicarazvan.websocketservice.models.ApprovePlanNotification;
import com.mocicarazvan.websocketservice.models.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovePlanNotificationMapper extends
        NotificationTemplateMapper<Plan, PlanResponse, ApprovedNotificationType, ApprovePlanNotification, ApprovePlanNotificationResponse> {
    private final PlanMapper planMapper;
    private final ConversationUserMapper conversationUserMapper;

    @Override
    public ApprovePlanNotificationResponse fromModelToResponse(ApprovePlanNotification approvePlanNotification) {
        return ApprovePlanNotificationResponse.builder()
                .id(approvePlanNotification.getId())
                .sender(conversationUserMapper.fromModelToResponse(approvePlanNotification.getSender()))
                .receiver(conversationUserMapper.fromModelToResponse(approvePlanNotification.getReceiver()))
                .type(approvePlanNotification.getType())
                .reference(planMapper.fromModelToResponse(approvePlanNotification.getReference()))
                .content(approvePlanNotification.getContent())
                .extraLink(approvePlanNotification.getExtraLink())
                .timestamp(approvePlanNotification.getTimestamp())
                .createdAt(approvePlanNotification.getCreatedAt())
                .updatedAt(approvePlanNotification.getUpdatedAt())
                .build();
    }
}

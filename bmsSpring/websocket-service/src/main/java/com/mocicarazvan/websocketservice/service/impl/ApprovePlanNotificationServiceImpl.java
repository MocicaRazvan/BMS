package com.mocicarazvan.websocketservice.service.impl;

import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationBody;
import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.mappers.ApprovePlanNotificationMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.ApprovePlanNotification;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.models.Plan;
import com.mocicarazvan.websocketservice.repositories.ApprovePlanNotificationRepository;
import com.mocicarazvan.websocketservice.repositories.PlanRepository;
import com.mocicarazvan.websocketservice.service.ApprovePlanNotificationService;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.service.generic.impl.ApproveNotificationServiceTemplateImpl;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
public class ApprovePlanNotificationServiceImpl
        extends ApproveNotificationServiceTemplateImpl
        <Plan, PlanResponse, ApprovePlanNotification, ApprovePlanNotificationBody, ApprovePlanNotificationResponse,
                PlanRepository, ApprovePlanNotificationRepository, ApprovePlanNotificationMapper>
        implements ApprovePlanNotificationService {


    public ApprovePlanNotificationServiceImpl(PlanRepository referenceRepository, ConversationUserService conversationUserService, SimpleAsyncTaskExecutor asyncExecutor, ApprovePlanNotificationRepository notificationTemplateRepository, ApprovePlanNotificationMapper notificationTemplateMapper, SimpMessagingTemplate messagingTemplate, CustomConvertAndSendToUser customConvertAndSendToUser) {
        super(referenceRepository, conversationUserService, "chat_plan", "approvePlanNotification", asyncExecutor, notificationTemplateRepository, notificationTemplateMapper, messagingTemplate, customConvertAndSendToUser);
    }

    @Override
    public Plan createApprovedReference(ApprovePlanNotificationBody body, Long appId, ConversationUser receiver) {
        return Plan.builder()
                .approved(body.getType().equals(ApprovedNotificationType.APPROVED))
                .appId(appId)
                .receiver(receiver)
                .id(body.getReferenceId())
                .build();
    }

    @Override
    protected ApprovePlanNotification createModelInstance(ConversationUser sender, ConversationUser receiver, ApprovedNotificationType type, Plan reference, String content, String extraLink) {
        return ApprovePlanNotification.builder()
                .sender(sender)
                .receiver(receiver)
                .type(type)
                .reference(reference)
                .content(content)
                .extraLink(extraLink)
                .build();
    }
}

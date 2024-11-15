package com.mocicarazvan.websocketservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.websocketservice.annotations.CustomRetryable;
import com.mocicarazvan.websocketservice.dtos.bought.BoughtNotificationBody;
import com.mocicarazvan.websocketservice.dtos.bought.BoughtNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.bought.InternalBoughtBody;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.enums.BoughtNotificationType;
import com.mocicarazvan.websocketservice.exceptions.notFound.EntityNotFound;
import com.mocicarazvan.websocketservice.mappers.BoughtNotificationMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.BoughtNotification;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.models.Plan;
import com.mocicarazvan.websocketservice.repositories.BoughtNotificationRepository;
import com.mocicarazvan.websocketservice.repositories.PlanRepository;
import com.mocicarazvan.websocketservice.service.BoughtNotificationService;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.service.generic.impl.NotificationTemplateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class BoughtNotificationServiceImpl
        extends NotificationTemplateServiceImpl<Plan, PlanResponse, BoughtNotificationType, BoughtNotification,
        BoughtNotificationBody, BoughtNotificationResponse, PlanRepository, BoughtNotificationRepository, BoughtNotificationMapper>


        implements BoughtNotificationService {


    private final ObjectMapper objectMapper;

    public BoughtNotificationServiceImpl(PlanRepository referenceRepository, ConversationUserService conversationUserService, SimpleAsyncTaskExecutor asyncExecutor, BoughtNotificationRepository notificationTemplateRepository, BoughtNotificationMapper notificationTemplateMapper, SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper, CustomConvertAndSendToUser customConvertAndSendToUser) {
        super(referenceRepository, conversationUserService, "chat_plan", "boughtNotification", asyncExecutor, notificationTemplateRepository, notificationTemplateMapper, messagingTemplate, customConvertAndSendToUser);

        this.objectMapper = objectMapper;
    }


    @Override
    protected BoughtNotification createModelInstance(ConversationUser sender, ConversationUser receiver, BoughtNotificationType type, Plan reference, String content, String extraLink) {
        return BoughtNotification.builder()
                .sender(sender)
                .receiver(receiver)
                .type(type)
                .reference(reference)
                .content(content)
                .extraLink(extraLink)
                .build();
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CustomRetryable
    public Void saveInternalNotifications(InternalBoughtBody internalBoughtBody) {
        CompletableFuture<ConversationUser> senderFuture = conversationUserService.getUserByEmailAsync(internalBoughtBody.getSenderEmail());
        List<CompletableFuture<BoughtNotificationResponse>> notificationsFuture = internalBoughtBody.getPlans().stream()
                .map(ip -> CompletableFuture.supplyAsync(() -> referenceRepository.findByAppIdAndApprovedTrue(Long.valueOf(ip.getId()))
                                        .orElseThrow(() -> new EntityNotFound(referenceName, Long.valueOf(ip.getId())))
                                , asyncExecutor)
                        .thenComposeAsync(plan -> {

                            String content;
                            try {
                                content = objectMapper.writeValueAsString(Map.of("title", ip.getTitle()));
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Could not serialize plan title");
                            }

                            BoughtNotificationBody boughtNotificationBody = BoughtNotificationBody.builder()
                                    .senderEmail(internalBoughtBody.getSenderEmail())
                                    .receiverEmail(plan.getReceiver().getEmail())
                                    .type(BoughtNotificationType.NEW_BOUGHT)
                                    .referenceId(plan.getId())
                                    .content(content)
                                    .build();
                            return CompletableFuture.supplyAsync(() -> saveNotification(boughtNotificationBody), asyncExecutor);
                        }, asyncExecutor)).toList();

        List<CompletableFuture<?>> allFutures = new ArrayList<>();
        allFutures.add(senderFuture);
        allFutures.addAll(notificationsFuture);
        return CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
    }


}

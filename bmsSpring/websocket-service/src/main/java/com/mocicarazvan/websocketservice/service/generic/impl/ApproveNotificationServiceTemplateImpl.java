package com.mocicarazvan.websocketservice.service.generic.impl;

import com.mocicarazvan.websocketservice.annotations.CustomRetryable;
import com.mocicarazvan.websocketservice.dtos.generic.ApproveNotificationBody;
import com.mocicarazvan.websocketservice.dtos.generic.ApproveResponse;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.enums.NotificationNotifyType;
import com.mocicarazvan.websocketservice.mappers.generic.NotificationTemplateMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.models.generic.ApprovedModel;
import com.mocicarazvan.websocketservice.models.generic.NotificationTemplate;
import com.mocicarazvan.websocketservice.repositories.generic.ApproveRepository;
import com.mocicarazvan.websocketservice.repositories.generic.NotificationTemplateRepository;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.service.generic.ApproveNotificationServiceTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class ApproveNotificationServiceTemplateImpl<R extends ApprovedModel,
        RRESP extends ApproveResponse,
        MODEL extends NotificationTemplate<R, ApprovedNotificationType>,
        BODY extends ApproveNotificationBody,
        RESPONSE extends NotificationTemplateResponse<RRESP, ApprovedNotificationType>,
        RREPO extends ApproveRepository<R>,
        MREOP extends NotificationTemplateRepository<R, ApprovedNotificationType, MODEL>,
        MMAP extends NotificationTemplateMapper<R, RRESP, ApprovedNotificationType, MODEL, RESPONSE>
        >
        extends NotificationTemplateServiceImpl<R, RRESP, ApprovedNotificationType, MODEL, BODY, RESPONSE, RREPO, MREOP, MMAP>
        implements ApproveNotificationServiceTemplate<R, RRESP, BODY, RESPONSE> {

    public ApproveNotificationServiceTemplateImpl(RREPO referenceRepository, ConversationUserService conversationUserService, String referenceName, String notificationName, SimpleAsyncTaskExecutor asyncExecutor, MREOP notificationTemplateRepository, MMAP notificationTemplateMapper, SimpMessagingTemplate messagingTemplate, CustomConvertAndSendToUser customConvertAndSendToUser) {
        super(referenceRepository, conversationUserService, referenceName, notificationName, asyncExecutor, notificationTemplateRepository, notificationTemplateMapper, messagingTemplate, customConvertAndSendToUser);
    }

    public abstract R createApprovedReference(BODY body, Long appId, ConversationUser receiver);

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public RESPONSE saveApprovedNotificationCreateReference(BODY body, Long appId) {
        List<MODEL> toBeDeleted = new ArrayList<>();

        R reference = referenceRepository.findByAppId(appId).orElse(null);
        if (reference != null) {
            toBeDeleted = notificationTemplateRepository.findAllByReferenceId(reference.getId());
            notificationTemplateRepository.deleteAllByReferenceId(reference.getId());
        }


        return CompletableFuture.allOf(
                        toBeDeleted.stream()
                                .map(n -> CompletableFuture.runAsync(() -> {
                                    RESPONSE response = notificationTemplateMapper.fromModelToResponse(n);
                                    notifyReceiver(response, NotificationNotifyType.REMOVED);
                                }, asyncExecutor))
                                .toArray(CompletableFuture[]::new))
                .thenComposeAsync(v -> fromBodyToApprovedModel(body, appId, reference), asyncExecutor)
                .thenApplyAsync(model -> {
                    RESPONSE response = notificationTemplateMapper.fromModelToResponse(notificationTemplateRepository.save(model));
                    notifyReceiver(response, NotificationNotifyType.ADDED);
                    return response;
                }, asyncExecutor).join();


    }


    public CompletableFuture<MODEL> fromBodyToApprovedModel(BODY body, Long appId, R aboveReference) {

        CompletableFuture<ConversationUser> senderFuture = conversationUserService.getUserByEmailAsync(body.getSenderEmail());
        CompletableFuture<ConversationUser> receiverFuture = conversationUserService.getUserByEmailAsync(body.getReceiverEmail());
        CompletableFuture<R> referenceFuture = CompletableFuture.completedFuture(aboveReference);


        return CompletableFuture.allOf(senderFuture, receiverFuture, referenceFuture)
                .thenApplyAsync(u -> {
                    try {
                        ConversationUser sender = senderFuture.get();
                        ConversationUser receiver = receiverFuture.get();
                        R reference = referenceFuture.get();
                        if (reference == null) {
                            reference = referenceRepository.save(createApprovedReference(body, appId, receiver));
                        } else {
                            reference.setApproved(body.getType().equals(ApprovedNotificationType.APPROVED));
                        }
                        return createModelInstance(sender, receiver, body.getType(), reference, body.getContent(), body.getExtraLink());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, asyncExecutor);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public void deleteByReferenceId(Long referenceId) {
        referenceRepository.findById(referenceId).ifPresent((r) -> {
            log.info("Deleting all notifications for reference id: {}", r.getId());
            notificationTemplateRepository.deleteAllByReferenceId(r.getId());
        });
    }

}

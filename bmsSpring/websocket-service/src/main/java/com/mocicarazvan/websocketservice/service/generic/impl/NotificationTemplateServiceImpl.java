package com.mocicarazvan.websocketservice.service.generic.impl;

import com.mocicarazvan.websocketservice.annotations.CustomRetryable;
import com.mocicarazvan.websocketservice.dtos.generic.IdResponse;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateBody;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateResponse;
import com.mocicarazvan.websocketservice.enums.NotificationNotifyType;
import com.mocicarazvan.websocketservice.exceptions.notFound.EntityNotFound;
import com.mocicarazvan.websocketservice.mappers.generic.NotificationTemplateMapper;
import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.models.generic.IdGenerated;
import com.mocicarazvan.websocketservice.models.generic.NotificationTemplate;
import com.mocicarazvan.websocketservice.repositories.generic.IdGeneratedRepository;
import com.mocicarazvan.websocketservice.repositories.generic.NotificationTemplateRepository;
import com.mocicarazvan.websocketservice.service.ConversationUserService;
import com.mocicarazvan.websocketservice.service.generic.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RequiredArgsConstructor

public abstract class NotificationTemplateServiceImpl<R extends IdGenerated, RRESP extends IdResponse, E extends Enum<E>,
        MODEL extends NotificationTemplate<R, E>, BODY extends NotificationTemplateBody<E>, RESPONSE extends NotificationTemplateResponse<RRESP, E>,
        RREPO extends IdGeneratedRepository<R>,
        MREOP extends NotificationTemplateRepository<R, E, MODEL>,
        MMAP extends NotificationTemplateMapper<R, RRESP, E, MODEL, RESPONSE>> implements NotificationTemplateService<R, RRESP, E, BODY, RESPONSE> {

    protected final RREPO referenceRepository;
    protected final ConversationUserService conversationUserService;
    protected final String referenceName;
    protected final String notificationName;
    protected final SimpleAsyncTaskExecutor asyncExecutor;
    protected final MREOP notificationTemplateRepository;
    protected final MMAP notificationTemplateMapper;
    protected final SimpMessagingTemplate messagingTemplate;
    private final CustomConvertAndSendToUser customConvertAndSendToUser;


    //    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @Override
    @CustomRetryable
    public RESPONSE saveNotification(BODY body) {

        return fromBodyToModel(body)
                .thenApplyAsync(model -> {

                            RESPONSE response = notificationTemplateMapper.fromModelToResponse(notificationTemplateRepository.save(model));
                            notifyReceiver(response, NotificationNotifyType.ADDED);
                            return response;
                        },
                        asyncExecutor)
                .join();
    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
//    @Override
//    @CustomRetryable
//    public RESPONSE saveNotificationCreateReference(BODY body, BiFunction<BODY, ConversationUser, R> createReference) {
//        return fromBodyToModel(body, createReference)
//                .thenApplyAsync(model -> {
//                            RESPONSE response = notificationTemplateMapper.fromModelToResponse(notificationTemplateRepository.save(model));
//                            notifyReceiver(response, NotificationNotifyType.ADDED);
//                            return response;
//                        },
//                        asyncExecutor)
//                .join();
//    }

    @Override
    public void deleteById(Long id) {
        MODEL m = getNotificationById(id);
        notificationTemplateRepository.delete(m);
        // todo mai bn stergi din front
        notifyReceiver(notificationTemplateMapper.fromModelToResponse(m), NotificationNotifyType.REMOVED);
    }


    @Override
//    @Transactional
    public List<RESPONSE> getAllBySenderEmailAndType(String senderEmail, E type) {
        Long senderId = conversationUserService.getUserByEmail(senderEmail).getId();
        return (type == null ? notificationTemplateRepository.findAllBySenderId(senderId)
                : notificationTemplateRepository.findAllBySenderIdAndType(senderId, type))
                .stream().map(notificationTemplateMapper::fromModelToResponse)
                .toList();
    }


    @Override
//    @Transactional

    public void deleteAllBySenderEmailAndType(String senderEmail, E type) {
        Long senderId = conversationUserService.getUserByEmail(senderEmail).getId();
        if (type == null) {
            notificationTemplateRepository.deleteAllBySenderId(senderId);
        } else {
            notificationTemplateRepository.deleteAllBySenderIdAndType(senderId, type);
        }
        // todo mai bn stergi din front
//        notifyReceiver(null, NotificationNotifyType.REMOVED);
    }


    //    @Transactional

    public CompletableFuture<MODEL> fromBodyToModel(BODY body) {

        CompletableFuture<ConversationUser> senderFuture = conversationUserService.getUserByEmailAsync(body.getSenderEmail());
        CompletableFuture<ConversationUser> receiverFuture = conversationUserService.getUserByEmailAsync(body.getReceiverEmail());
        CompletableFuture<R> referenceFuture = referenceRepository.findById(body.getReferenceId())
                .map(CompletableFuture::completedFuture)
                .orElseThrow(() -> new EntityNotFound(referenceName, body.getReferenceId()));

        return createResponseFutures(body, senderFuture, receiverFuture, referenceFuture);
    }


    protected CompletableFuture<MODEL> createResponseFutures(BODY body, CompletableFuture<ConversationUser> senderFuture, CompletableFuture<ConversationUser> receiverFuture, CompletableFuture<R> referenceFuture) {
        return CompletableFuture.allOf(senderFuture, receiverFuture, referenceFuture)
                .thenApplyAsync(u -> {
                    try {
                        ConversationUser sender = senderFuture.get();
                        ConversationUser receiver = receiverFuture.get();
                        R reference = referenceFuture.get();
                        return createModelInstance(sender, receiver, body.getType(), reference, body.getContent(), body.getExtraLink());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, asyncExecutor);
    }

    public MODEL getNotificationById(Long id) {
        return notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFound("Notification", id));
    }

    public void notifyReceiver(RESPONSE response, NotificationNotifyType notificationNotifyType) {
        String type = notificationNotifyType.name().toLowerCase();
//        messagingTemplate.convertAndSendToUser(response.getReceiver().getEmail(), "/queue/notification/" + notificationName + "/" + type, response);
        customConvertAndSendToUser.sendToUser(response.getReceiver().getEmail(), "/queue/notification-" + notificationName + "-" + type, response);


    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public List<RESPONSE> getAllByReceiverEmailAndType(String receiverEmail, E type) {
        Long receiverId = conversationUserService.getOrCreateUserByEmail(receiverEmail).getId();
        return (type == null ? notificationTemplateRepository.findAllByReceiverId(receiverId)
                : notificationTemplateRepository.findAllByReceiverIdAndType(receiverId, type))
                .stream().map(notificationTemplateMapper::fromModelToResponse)
                .toList();
    }


    @Override
//    @Transactional
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public void deleteAllByReceiverEmailAndType(String senderEmail, E type) {
        log.info("Deleting all notifications for receiver email: {}", senderEmail);
        Long receiverId = conversationUserService.getUserByEmail(senderEmail).getId();
        if (type == null) {
            notificationTemplateRepository.deleteAllByReceiverId(receiverId);
        } else {
            notificationTemplateRepository.deleteAllByReceiverIdAndType(receiverId, type);
        }
        // todo mai bn stergi din front
    }

    @Override
//    @Transactional
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CustomRetryable
    public void deleteAllByReceiverEmailAndTypeRemoveReference(String senderEmail, E type, Long referenceId) {
        deleteAllByReceiverEmailAndType(senderEmail, type);
        referenceRepository.deleteById(referenceId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
//    @Transactional
    @CustomRetryable
    public void deleteAllByReceiverEmailSenderEmailAndType(String senderEmail, String receiverEmail, E type) {
        CompletableFuture<ConversationUser> senderFuture = conversationUserService.getUserByEmailAsync(senderEmail);
        CompletableFuture<ConversationUser> receiverFuture = conversationUserService.getUserByEmailAsync(receiverEmail);

        Map<String, ConversationUser> userMap = CompletableFuture.allOf(senderFuture, receiverFuture)
                .thenApplyAsync(v -> {
                    try {
                        Map<String, ConversationUser> users = new HashMap<>();
                        users.put("sender", senderFuture.get());
                        users.put("receiver", receiverFuture.get());
                        return users;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, asyncExecutor).join();


        ConversationUser sender = userMap.get("sender");
        ConversationUser receiver = userMap.get("receiver");

        if (type == null) {
            notificationTemplateRepository.deleteAllBySenderIdAndReceiverId(sender.getId(), receiver.getId());
        } else {
            notificationTemplateRepository.deleteAllBySenderIdAndReceiverIdAndType(sender.getId(), receiver.getId(), type);
        }
    }


    public CompletableFuture<Void> notifyDeleteByReferenceId(Long referenceId, List<String> receiverEmails) {
        return CompletableFuture
                .runAsync(() -> CompletableFuture.allOf(
                        receiverEmails.stream()
                                .map(email -> CompletableFuture.runAsync(() ->
//                                        messagingTemplate.convertAndSendToUser(email, "/queue/notification/" + notificationName + "/removed", referenceId.toString()), asyncExecutor))
                                        customConvertAndSendToUser.sendToUser(email, "/queue/notification-" + notificationName + "-removed", referenceId.toString()), asyncExecutor))
                                .toArray(CompletableFuture[]::new)
                ).join(), asyncExecutor);
    }


    protected abstract MODEL createModelInstance(ConversationUser sender, ConversationUser receiver, E type, R reference, String content, String extraLink);

}

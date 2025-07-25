package com.mocicarazvan.websocketservice.service.generic;

import com.mocicarazvan.websocketservice.dtos.generic.IdResponse;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateBody;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateResponse;
import com.mocicarazvan.websocketservice.models.generic.IdGenerated;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NotificationTemplateService<R extends IdGenerated, RRESP extends IdResponse, E extends Enum<E>,
        BODY extends NotificationTemplateBody<E>, RESPONSE extends NotificationTemplateResponse<RRESP, E>> {

    RESPONSE saveNotification(BODY body);


//    RESPONSE saveNotificationCreateReference(BODY body, BiFunction<BODY, ConversationUser, R> createReference);

    void deleteById(Long id);

    List<RESPONSE> getAllBySenderEmailAndType(String senderEmail, E type);

    void deleteAllBySenderEmailAndType(String senderEmail, E type);

    List<RESPONSE> getAllByReceiverEmailAndType(String senderEmail, E type);

    void deleteAllByReceiverEmailAndType(String senderEmail, E type);

    //    @Transactional
    void deleteAllByReceiverEmailAndTypeRemoveReference(String senderEmail, E type, Long referenceId);

    void deleteAllByReceiverEmailSenderEmailAndType(String senderEmail, String receiverEmail, E type);

    CompletableFuture<Void> notifyDeleteByReferenceId(Long referenceId, List<String> receiverEmails);
}

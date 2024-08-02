package com.mocicarazvan.websocketservice.controllers.generics;


import com.mocicarazvan.websocketservice.dtos.generic.IdResponse;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateBody;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateResponse;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderEmailReceiverEmailDto;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderTypeDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface NotificationTemplateController<RRESP extends IdResponse, E extends Enum<E>,
        BODY extends NotificationTemplateBody<E>, RESPONSE extends NotificationTemplateResponse<RRESP, E>> {


    void sendNotification(BODY body);

    void deleteById(Long id);

    ResponseEntity<List<RESPONSE>> getAllBySenderEmailAndType(SenderTypeDto<E> senderTypeDto);

    void deleteAllBySenderEmailAndType(SenderTypeDto<E> senderTypeDto);

    ResponseEntity<List<RESPONSE>> getAllByReceiverEmailAndType(SenderTypeDto<E> senderTypeDto);

    void deleteAllByReceiverEmailAndType(SenderTypeDto<E> senderTypeDto);

//    void deleteAllBySenderIdReceiverIdAndType(Long senderId, Long receiverId, E type);

    void deleteAllByReceiverEmailSenderEmail(SenderEmailReceiverEmailDto senderEmailReceiverEmailDto);
}

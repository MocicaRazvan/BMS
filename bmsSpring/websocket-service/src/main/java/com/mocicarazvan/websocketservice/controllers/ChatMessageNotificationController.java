package com.mocicarazvan.websocketservice.controllers;

import com.mocicarazvan.websocketservice.controllers.generics.NotificationTemplateController;
import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.dtos.notifications.ChatMessageNotificationBody;
import com.mocicarazvan.websocketservice.dtos.notifications.ChatMessageNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderEmailReceiverEmailDto;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderTypeDto;
import com.mocicarazvan.websocketservice.enums.ChatMessageNotificationType;
import com.mocicarazvan.websocketservice.service.ChatMessageNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RequiredArgsConstructor
@RestController
public class ChatMessageNotificationController implements
        NotificationTemplateController<ChatRoomResponse, ChatMessageNotificationType, ChatMessageNotificationBody, ChatMessageNotificationResponse> {


    private final ChatMessageNotificationService chatMessageNotificationService;

    @MessageMapping("/chatMessageNotification/sendNotification")
    @Override
    public void sendNotification(@Valid @Payload ChatMessageNotificationBody body) {
        chatMessageNotificationService.saveNotification(body);
    }

    @MessageMapping("/chatMessageNotification/deleteNotification/{id}")
    @Override
    public void deleteById(@DestinationVariable Long id) {
        chatMessageNotificationService.deleteById(id);
    }

    @PatchMapping("/chatMessageNotification/getAllBySenderEmailAndType")
    @Override
    public ResponseEntity<List<ChatMessageNotificationResponse>> getAllBySenderEmailAndType(@Valid @RequestBody SenderTypeDto<ChatMessageNotificationType> senderTypeDto) {
        return ResponseEntity.ok(chatMessageNotificationService.getAllBySenderEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType()));
    }

    @MessageMapping("/chatMessageNotification/deleteAllBySenderEmailAndType")
    @Override
    public void deleteAllBySenderEmailAndType(@Valid @Payload SenderTypeDto<ChatMessageNotificationType> senderTypeDto) {
        chatMessageNotificationService.deleteAllBySenderEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType());
    }

    @PatchMapping("/chatMessageNotification/getAllByReceiverEmailAndType")
    @Override
    public ResponseEntity<List<ChatMessageNotificationResponse>> getAllByReceiverEmailAndType(@Valid @RequestBody SenderTypeDto<ChatMessageNotificationType> senderTypeDto) {
        return ResponseEntity.ok(chatMessageNotificationService.getAllByReceiverEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType()));
    }

    @MessageMapping("/chatMessageNotification/deleteAllByReceiverEmailAndType")
    @Override
    public void deleteAllByReceiverEmailAndType(@Valid @Payload SenderTypeDto<ChatMessageNotificationType> senderTypeDto) {
        chatMessageNotificationService.deleteAllByReceiverEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType());
    }

    @MessageMapping("/chatMessageNotification/deleteAllByReceiverEmailSenderEmail")
    @Override
    public void deleteAllByReceiverEmailSenderEmail(@Valid @Payload SenderEmailReceiverEmailDto senderEmailReceiverEmailDto) {
        chatMessageNotificationService.deleteAllByReceiverEmailSenderEmailAndType(senderEmailReceiverEmailDto.getSenderEmail(), senderEmailReceiverEmailDto.getReceiverEmail(), null);
    }


}

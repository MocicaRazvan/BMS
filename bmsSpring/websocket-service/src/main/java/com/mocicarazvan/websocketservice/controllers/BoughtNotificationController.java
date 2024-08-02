package com.mocicarazvan.websocketservice.controllers;


import com.mocicarazvan.websocketservice.controllers.generics.NotificationTemplateController;
import com.mocicarazvan.websocketservice.dtos.bought.BoughtNotificationBody;
import com.mocicarazvan.websocketservice.dtos.bought.BoughtNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.bought.InternalBoughtBody;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderEmailReceiverEmailDto;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderTypeDto;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.enums.BoughtNotificationType;
import com.mocicarazvan.websocketservice.service.BoughtNotificationService;
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

@RestController
@RequiredArgsConstructor
public class BoughtNotificationController
        implements NotificationTemplateController<PlanResponse, BoughtNotificationType, BoughtNotificationBody, BoughtNotificationResponse> {
    private final BoughtNotificationService boughtNotificationService;

    @MessageMapping("/boughtNotification/sendNotification")
    @Override
    public void sendNotification(@Payload BoughtNotificationBody body) {
        boughtNotificationService.saveNotification(body);
    }

    @MessageMapping("/boughtNotification/deleteNotification/{id}")
    @Override
    public void deleteById(@DestinationVariable Long id) {
        boughtNotificationService.deleteById(id);
    }

    @PatchMapping("/boughtNotification/getAllBySenderEmailAndType")
    @Override
    public ResponseEntity<List<BoughtNotificationResponse>> getAllBySenderEmailAndType(@Valid @RequestBody SenderTypeDto<BoughtNotificationType> senderTypeDto) {
        return ResponseEntity.ok(boughtNotificationService.getAllBySenderEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType()));
    }

    @MessageMapping("/boughtNotification/deleteAllBySenderEmailAndType")
    @Override
    public void deleteAllBySenderEmailAndType(@Payload SenderTypeDto<BoughtNotificationType> senderTypeDto) {
        boughtNotificationService.deleteAllBySenderEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType());
    }

    @PatchMapping("/boughtNotification/getAllByReceiverEmailAndType")
    @Override
    public ResponseEntity<List<BoughtNotificationResponse>> getAllByReceiverEmailAndType(@Valid @RequestBody SenderTypeDto<BoughtNotificationType> senderTypeDto) {
        return ResponseEntity.ok(boughtNotificationService.getAllByReceiverEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType()));
    }

    @MessageMapping("/boughtNotification/deleteAllByReceiverEmailAndType")
    @Override
    public void deleteAllByReceiverEmailAndType(@Payload SenderTypeDto<BoughtNotificationType> senderTypeDto) {
        boughtNotificationService.deleteAllByReceiverEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType());
    }

    @MessageMapping("/boughtNotification/deleteAllByReceiverEmailSenderEmail")
    @Override
    public void deleteAllByReceiverEmailSenderEmail(@Payload SenderEmailReceiverEmailDto senderEmailReceiverEmailDto) {
        boughtNotificationService.deleteAllByReceiverEmailSenderEmailAndType(senderEmailReceiverEmailDto.getSenderEmail(), senderEmailReceiverEmailDto.getReceiverEmail(), null);
    }

    @PatchMapping("/boughtNotification/internal/sendNotifications")
    public ResponseEntity<Void> saveInternalNotifications(@Valid @RequestBody InternalBoughtBody internalBoughtBody) {
        boughtNotificationService.saveInternalNotifications(internalBoughtBody);
        return ResponseEntity.noContent().build();
    }
}

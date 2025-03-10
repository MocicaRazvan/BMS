package com.mocicarazvan.websocketservice.controllers;

import com.mocicarazvan.websocketservice.controllers.generics.ApproveNotificationTemplateController;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderEmailReceiverEmailDto;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderTypeDto;
import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationBody;
import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.service.ApprovePlanNotificationService;
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

public class ApprovePlanNotificationController implements ApproveNotificationTemplateController<PlanResponse, ApprovePlanNotificationBody,
        ApprovePlanNotificationResponse> {

    private final ApprovePlanNotificationService approvePlanNotificationService;

    @Override
    @MessageMapping("/approvePlanNotification/sendNotificationCreateReference/{appId}")
    public void sendNotificationCreateReference(@Valid @Payload ApprovePlanNotificationBody body,
                                                @DestinationVariable Long appId) {
        approvePlanNotificationService.saveApprovedNotificationCreateReference(body, appId);
    }

    @Override
    @MessageMapping("/approvePlanNotification/deleteByReferenceId/{referenceId}")
    public void deleteByReferenceId(@Valid @DestinationVariable Long referenceId) {
        approvePlanNotificationService.deleteByReferenceId(referenceId);
    }

    @Override
    @MessageMapping("/approvePlanNotification/sendNotification")
    public void sendNotification(@Valid @Payload ApprovePlanNotificationBody body) {
        approvePlanNotificationService.saveNotification(body);
    }

    @Override
    @MessageMapping("/approvePlanNotification/deleteNotification/{id}")
    public void deleteById(@DestinationVariable Long id) {
        approvePlanNotificationService.deleteById(id);
    }

    @Override
    @PatchMapping("/approvePlanNotification/getAllBySenderEmailAndType")
    public ResponseEntity<List<ApprovePlanNotificationResponse>> getAllBySenderEmailAndType(@Valid @RequestBody SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        return ResponseEntity.ok(approvePlanNotificationService.getAllBySenderEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType()));
    }

    @Override
    @MessageMapping("/approvePlanNotification/deleteAllBySenderEmailAndType")
    public void deleteAllBySenderEmailAndType(@Valid @Payload SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        approvePlanNotificationService.deleteAllBySenderEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType());
    }

    @Override
    @PatchMapping("/approvePlanNotification/getAllByReceiverEmailAndType")
    public ResponseEntity<List<ApprovePlanNotificationResponse>> getAllByReceiverEmailAndType(@Valid @RequestBody SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        return ResponseEntity.ok(approvePlanNotificationService.getAllByReceiverEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType()));
    }

    @Override
    @MessageMapping("/approvePlanNotification/deleteAllByReceiverEmailAndType")
    public void deleteAllByReceiverEmailAndType(@Valid @Payload SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        approvePlanNotificationService.deleteAllByReceiverEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType());
    }

    @Override
    @MessageMapping("/approvePlanNotification/deleteAllByReceiverEmailSenderEmail")
    public void deleteAllByReceiverEmailSenderEmail(@Valid @Payload SenderEmailReceiverEmailDto senderEmailReceiverEmailDto) {
        approvePlanNotificationService.deleteAllByReceiverEmailSenderEmailAndType(senderEmailReceiverEmailDto.getSenderEmail(), senderEmailReceiverEmailDto.getReceiverEmail(), null);
    }
}

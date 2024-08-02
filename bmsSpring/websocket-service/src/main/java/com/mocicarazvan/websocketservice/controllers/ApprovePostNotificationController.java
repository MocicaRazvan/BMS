package com.mocicarazvan.websocketservice.controllers;

import com.mocicarazvan.websocketservice.controllers.generics.ApproveNotificationTemplateController;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderEmailReceiverEmailDto;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderTypeDto;
import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationBody;
import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.post.PostResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.service.ApprovePostNotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
//@Controller
@RestController

public class ApprovePostNotificationController implements ApproveNotificationTemplateController<PostResponse, ApprovePostNotificationBody, ApprovePostNotificationResponse> {

    private final ApprovePostNotificationService approvePostNotificationService;

    @Override
    @MessageMapping("/approvePostNotification/sendNotificationCreateReference/{appId}")
    public void sendNotificationCreateReference(@Payload ApprovePostNotificationBody body,
                                                @DestinationVariable Long appId) {
        approvePostNotificationService.saveApprovedNotificationCreateReference(body, appId);
    }

    @Override
    @MessageMapping("/approvePostNotification/deleteByReferenceId/{referenceId}")
    public void deleteByReferenceId(@DestinationVariable Long referenceId) {
        approvePostNotificationService.deleteByReferenceId(referenceId);
    }

    @Override
    @MessageMapping("/approvePostNotification/sendNotification")
    public void sendNotification(@Payload ApprovePostNotificationBody body) {
        approvePostNotificationService.saveNotification(body);
    }

    @Override
    @MessageMapping("/approvePostNotification/deleteNotification/{id}")
    public void deleteById(@DestinationVariable Long id) {
        approvePostNotificationService.deleteById(id);
    }

    @Override
    @PatchMapping("/approvePostNotification/getAllBySenderEmailAndType")
    public ResponseEntity<List<ApprovePostNotificationResponse>> getAllBySenderEmailAndType(@Valid @RequestBody SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        return ResponseEntity.ok(approvePostNotificationService.getAllBySenderEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType()));
    }

    @Override
    @MessageMapping("/approvePostNotification/deleteAllBySenderEmailAndType")
    public void deleteAllBySenderEmailAndType(@Payload SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        approvePostNotificationService.deleteAllBySenderEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType());
    }

    @Override
    @PatchMapping("/approvePostNotification/getAllByReceiverEmailAndType")
    public ResponseEntity<List<ApprovePostNotificationResponse>> getAllByReceiverEmailAndType(@Valid @RequestBody SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        return ResponseEntity.ok(approvePostNotificationService.getAllByReceiverEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType()));
    }

    @Override
    @MessageMapping("/approvePostNotification/deleteAllByReceiverEmailAndType")
    public void deleteAllByReceiverEmailAndType(@Payload SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        approvePostNotificationService.deleteAllByReceiverEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType());
    }

    @Override
    @MessageMapping("/approvePostNotification/deleteAllByReceiverEmailSenderEmail")
    public void deleteAllByReceiverEmailSenderEmail(@Payload SenderEmailReceiverEmailDto senderEmailReceiverEmailDto) {
        approvePostNotificationService.deleteAllByReceiverEmailSenderEmailAndType(senderEmailReceiverEmailDto.getSenderEmail(), senderEmailReceiverEmailDto.getReceiverEmail(), null);
    }
}

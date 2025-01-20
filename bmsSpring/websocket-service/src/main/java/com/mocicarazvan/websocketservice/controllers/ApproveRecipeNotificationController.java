package com.mocicarazvan.websocketservice.controllers;

import com.mocicarazvan.websocketservice.controllers.generics.ApproveNotificationTemplateController;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderEmailReceiverEmailDto;
import com.mocicarazvan.websocketservice.dtos.notifications.SenderTypeDto;
import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationBody;
import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.service.ApproveRecipeNotificationService;
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

public class ApproveRecipeNotificationController implements ApproveNotificationTemplateController<RecipeResponse, ApproveRecipeNotificationBody, ApproveRecipeNotificationResponse> {

    private final ApproveRecipeNotificationService approveRecipeNotificationService;

    @Override
    @MessageMapping("/approveRecipeNotification/sendNotificationCreateReference/{appId}")
    public void sendNotificationCreateReference(@Payload ApproveRecipeNotificationBody body,
                                                @DestinationVariable Long appId) {
        approveRecipeNotificationService.saveApprovedNotificationCreateReference(body, appId);
    }

    @Override
    @MessageMapping("/approveRecipeNotification/deleteByReferenceId/{referenceId}")
    public void deleteByReferenceId(@DestinationVariable Long referenceId) {
        approveRecipeNotificationService.deleteByReferenceId(referenceId);
    }

    @Override
    @MessageMapping("/approveRecipeNotification/sendNotification")
    public void sendNotification(@Payload ApproveRecipeNotificationBody body) {
        approveRecipeNotificationService.saveNotification(body);
    }

    @Override
    @MessageMapping("/approveRecipeNotification/deleteNotification/{id}")
    public void deleteById(@DestinationVariable Long id) {
        approveRecipeNotificationService.deleteById(id);
    }

    @Override
    @PatchMapping("/approveRecipeNotification/getAllBySenderEmailAndType")
    public ResponseEntity<List<ApproveRecipeNotificationResponse>> getAllBySenderEmailAndType(@Valid @RequestBody SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        return ResponseEntity.ok(approveRecipeNotificationService.getAllBySenderEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType()));
    }

    @Override
    @MessageMapping("/approveRecipeNotification/deleteAllBySenderEmailAndType")
    public void deleteAllBySenderEmailAndType(@Payload SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        approveRecipeNotificationService.deleteAllBySenderEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType());
    }

    @Override
    @PatchMapping("/approveRecipeNotification/getAllByReceiverEmailAndType")
    public ResponseEntity<List<ApproveRecipeNotificationResponse>> getAllByReceiverEmailAndType(@Valid @RequestBody SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        return ResponseEntity.ok(approveRecipeNotificationService.getAllByReceiverEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType()));
    }

    @Override
    @MessageMapping("/approveRecipeNotification/deleteAllByReceiverEmailAndType")
    public void deleteAllByReceiverEmailAndType(@Payload SenderTypeDto<ApprovedNotificationType> senderTypeDto) {
        approveRecipeNotificationService.deleteAllByReceiverEmailAndType(senderTypeDto.getSenderEmail(), senderTypeDto.getType());
    }

    @Override
    @MessageMapping("/approveRecipeNotification/deleteAllByReceiverEmailSenderEmail")
    public void deleteAllByReceiverEmailSenderEmail(@Payload SenderEmailReceiverEmailDto senderEmailReceiverEmailDto) {
        approveRecipeNotificationService.deleteAllByReceiverEmailSenderEmailAndType(senderEmailReceiverEmailDto.getSenderEmail(), senderEmailReceiverEmailDto.getReceiverEmail(), null);
    }
}

package com.mocicarazvan.websocketservice.listeners;


import com.mocicarazvan.websocketservice.dtos.generic.ApproveNotificationBody;
import com.mocicarazvan.websocketservice.dtos.generic.ApproveResponse;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.models.generic.ApprovedModel;
import com.mocicarazvan.websocketservice.service.generic.ApproveNotificationServiceTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class ApproveQueListener<R extends ApprovedModel, RRESP extends ApproveResponse,
        BODY extends ApproveNotificationBody, RESPONSE extends NotificationTemplateResponse<RRESP, ApprovedNotificationType>> {

    private final ApproveNotificationServiceTemplate<R, RRESP, BODY, RESPONSE> approvePostNotificationService;

    public void listen(BODY message) {
        //todo if is null notify for all
        if (message.getReferenceId() == null) {
            throw new IllegalArgumentException("ReferenceId is required for rabbitmq message");
        }

        approvePostNotificationService.saveApprovedNotificationCreateReference(
                message, message.getReferenceId()
        );
    }
}
package com.mocicarazvan.websocketservice.controllers.generics;

import com.mocicarazvan.websocketservice.dtos.generic.ApproveNotificationBody;
import com.mocicarazvan.websocketservice.dtos.generic.ApproveResponse;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;

public interface ApproveNotificationTemplateController<RRESP extends ApproveResponse,
        BODY extends ApproveNotificationBody, RESPONSE extends NotificationTemplateResponse<RRESP, ApprovedNotificationType>>
        extends NotificationTemplateController<RRESP, ApprovedNotificationType, BODY, RESPONSE> {
    void sendNotificationCreateReference(BODY body, Long appId);

    void deleteByReferenceId(Long referenceId);
}

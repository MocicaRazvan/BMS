package com.mocicarazvan.websocketservice.service.generic;

import com.mocicarazvan.websocketservice.dtos.generic.ApproveNotificationBody;
import com.mocicarazvan.websocketservice.dtos.generic.ApproveResponse;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.models.generic.ApprovedModel;

public interface ApproveNotificationServiceTemplate<R extends ApprovedModel, RRESP extends ApproveResponse,
        BODY extends ApproveNotificationBody, RESPONSE extends NotificationTemplateResponse<RRESP, ApprovedNotificationType>
        > extends NotificationTemplateService<R, RRESP, ApprovedNotificationType, BODY, RESPONSE> {


    RESPONSE saveApprovedNotificationCreateReference(BODY body, Long appId);

    void deleteByReferenceId(Long referenceId);
}

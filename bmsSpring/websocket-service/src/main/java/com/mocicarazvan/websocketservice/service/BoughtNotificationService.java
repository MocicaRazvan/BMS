package com.mocicarazvan.websocketservice.service;

import com.mocicarazvan.websocketservice.dtos.bought.BoughtNotificationBody;
import com.mocicarazvan.websocketservice.dtos.bought.BoughtNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.bought.InternalBoughtBody;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.enums.BoughtNotificationType;
import com.mocicarazvan.websocketservice.models.Plan;
import com.mocicarazvan.websocketservice.service.generic.NotificationTemplateService;

public interface BoughtNotificationService extends NotificationTemplateService<Plan, PlanResponse, BoughtNotificationType, BoughtNotificationBody, BoughtNotificationResponse> {
    Void saveInternalNotifications(InternalBoughtBody internalBoughtBody);
}

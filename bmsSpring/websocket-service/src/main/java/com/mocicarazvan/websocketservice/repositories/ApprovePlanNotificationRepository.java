package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.models.ApprovePlanNotification;
import com.mocicarazvan.websocketservice.models.Plan;
import com.mocicarazvan.websocketservice.repositories.generic.NotificationTemplateRepository;


public interface ApprovePlanNotificationRepository extends NotificationTemplateRepository<Plan, ApprovedNotificationType, ApprovePlanNotification> {


}

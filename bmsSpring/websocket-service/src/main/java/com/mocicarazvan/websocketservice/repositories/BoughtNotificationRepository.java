package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.enums.BoughtNotificationType;
import com.mocicarazvan.websocketservice.models.BoughtNotification;
import com.mocicarazvan.websocketservice.models.Plan;
import com.mocicarazvan.websocketservice.repositories.generic.NotificationTemplateRepository;

public interface BoughtNotificationRepository extends NotificationTemplateRepository<Plan, BoughtNotificationType, BoughtNotification> {
}

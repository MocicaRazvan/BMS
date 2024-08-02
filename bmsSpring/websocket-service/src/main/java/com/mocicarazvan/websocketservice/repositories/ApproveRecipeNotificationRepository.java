package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.models.ApproveRecipeNotification;
import com.mocicarazvan.websocketservice.models.Recipe;
import com.mocicarazvan.websocketservice.repositories.generic.NotificationTemplateRepository;

public interface ApproveRecipeNotificationRepository extends NotificationTemplateRepository<Recipe, ApprovedNotificationType, ApproveRecipeNotification> {
}

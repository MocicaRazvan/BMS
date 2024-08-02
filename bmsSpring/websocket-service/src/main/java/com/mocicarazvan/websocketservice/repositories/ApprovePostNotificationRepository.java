package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.models.ApprovePostNotification;
import com.mocicarazvan.websocketservice.models.Post;
import com.mocicarazvan.websocketservice.repositories.generic.NotificationTemplateRepository;


public interface ApprovePostNotificationRepository extends NotificationTemplateRepository<Post, ApprovedNotificationType, ApprovePostNotification> {


}

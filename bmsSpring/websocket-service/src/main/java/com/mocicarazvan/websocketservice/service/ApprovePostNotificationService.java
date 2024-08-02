package com.mocicarazvan.websocketservice.service;

import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationBody;
import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.post.PostResponse;
import com.mocicarazvan.websocketservice.models.Post;
import com.mocicarazvan.websocketservice.service.generic.ApproveNotificationServiceTemplate;

public interface ApprovePostNotificationService extends ApproveNotificationServiceTemplate<Post, PostResponse,
        ApprovePostNotificationBody, ApprovePostNotificationResponse
        > {
}

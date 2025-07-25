package com.mocicarazvan.websocketservice.listeners;

import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationBody;
import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.post.PostResponse;
import com.mocicarazvan.websocketservice.models.Post;
import com.mocicarazvan.websocketservice.service.ApprovePostNotificationService;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PostApproveQueListener
        extends ApproveQueListener<Post, PostResponse, ApprovePostNotificationBody, ApprovePostNotificationResponse> {


    public PostApproveQueListener(ApprovePostNotificationService approvePostNotificationService) {
        super(approvePostNotificationService);
    }

    @RabbitListener(queues = "#{@environment['post.queue.name']}")
    public void listen(@Valid @Payload ApprovePostNotificationBody message) {
        super.listen(message);
    }

}



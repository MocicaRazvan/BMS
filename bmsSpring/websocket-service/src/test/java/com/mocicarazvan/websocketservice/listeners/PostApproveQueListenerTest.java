package com.mocicarazvan.websocketservice.listeners;

import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationBody;
import com.mocicarazvan.websocketservice.dtos.post.ApprovePostNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.post.PostResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.models.Post;
import com.mocicarazvan.websocketservice.service.ApprovePostNotificationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {PostApproveQueListener.class})
class PostApproveQueListenerTest extends BaseApproveListenerTest<Post, PostResponse, ApprovePostNotificationBody, ApprovePostNotificationResponse> {

    @MockBean
    ApprovePostNotificationService approvePostNotificationService;

    @Value("${post.queue.name}")
    String postQueueName;

    protected PostApproveQueListenerTest() {
        super(ApprovePostNotificationBody.class);
    }

    @Override
    protected ApprovePostNotificationBody createBody() {
        return ApprovePostNotificationBody.builder()
                .senderEmail("sender@example.com")
                .receiverEmail("receiver@example.com")
                .type(ApprovedNotificationType.APPROVED)
                .referenceId(1L)
                .content("content")
                .extraLink("extraLink")
                .build();
    }

    @Override
    protected ApprovePostNotificationBody createInvalidBody() {
        return ApprovePostNotificationBody.builder()
                .senderEmail("sender@example.com")
                .receiverEmail("receiver@example.com")
                .type(ApprovedNotificationType.APPROVED)
                .content("content")
                .extraLink("extraLink")
                .build();
    }

    @Override
    protected ApprovePostNotificationResponse createResponse() {
        return new ApprovePostNotificationResponse();
    }

    @PostConstruct
    void init() {
        setQueueName(postQueueName);
        setServiceTemplate(approvePostNotificationService);
    }
}
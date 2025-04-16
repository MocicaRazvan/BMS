package com.mocicarazvan.websocketservice.listeners;

import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationBody;
import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.models.Plan;
import com.mocicarazvan.websocketservice.service.ApprovePlanNotificationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {PlanApproveQueListener.class})
class PlanApproveQueListenerTest extends BaseApproveListenerTest<
        Plan, PlanResponse, ApprovePlanNotificationBody, ApprovePlanNotificationResponse
        > {

    @MockBean
    ApprovePlanNotificationService approvePlanNotificationService;

    @Value("${plan.queue.name}")
    private String planQueueName;


    protected PlanApproveQueListenerTest() {
        super(ApprovePlanNotificationBody.class);
    }

    @Override
    protected ApprovePlanNotificationBody createBody() {
        return ApprovePlanNotificationBody.builder()
                .senderEmail("sender@example.com")
                .receiverEmail("receiver@example.com")
                .type(ApprovedNotificationType.APPROVED)
                .referenceId(1L)
                .content("content")
                .extraLink("extraLink")
                .build();

    }

    @Override
    protected ApprovePlanNotificationBody createInvalidBody() {
        return ApprovePlanNotificationBody.builder()
                .senderEmail("sender@example.com")
                .receiverEmail("receiver@example.com")
                .type(ApprovedNotificationType.APPROVED)
                .content("content")
                .extraLink("extraLink")
                .build();

    }

    @Override
    protected ApprovePlanNotificationResponse createResponse() {
        return new ApprovePlanNotificationResponse();
    }

    @PostConstruct
    void init() {
        setQueueName(planQueueName);
        setServiceTemplate(approvePlanNotificationService);
    }

}
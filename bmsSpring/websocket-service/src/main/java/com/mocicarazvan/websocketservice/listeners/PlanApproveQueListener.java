package com.mocicarazvan.websocketservice.listeners;


import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationBody;
import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.models.Plan;
import com.mocicarazvan.websocketservice.service.ApprovePlanNotificationService;
import com.mocicarazvan.websocketservice.service.ApprovePostNotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component

public class PlanApproveQueListener
        extends ApproveQueListener<Plan, PlanResponse, ApprovePlanNotificationBody, ApprovePlanNotificationResponse> {


    public PlanApproveQueListener(ApprovePlanNotificationService approvePlanNotificationService) {
        super(approvePlanNotificationService);
    }

    @RabbitListener(queues = "#{@environment['plan.queue.name']}")
    public void listen(ApprovePlanNotificationBody message) {
        super.listen(message);
    }

}

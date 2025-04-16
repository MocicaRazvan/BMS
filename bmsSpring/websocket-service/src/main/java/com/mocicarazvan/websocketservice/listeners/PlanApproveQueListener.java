package com.mocicarazvan.websocketservice.listeners;


import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationBody;
import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.models.Plan;
import com.mocicarazvan.websocketservice.service.ApprovePlanNotificationService;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PlanApproveQueListener
        extends ApproveQueListener<Plan, PlanResponse, ApprovePlanNotificationBody, ApprovePlanNotificationResponse> {


    public PlanApproveQueListener(ApprovePlanNotificationService approvePlanNotificationService) {
        super(approvePlanNotificationService);
    }

    @RabbitListener(queues = "#{@environment['plan.queue.name']}")
    public void listen(@Valid @Payload ApprovePlanNotificationBody message) {
        super.listen(message);
    }

}

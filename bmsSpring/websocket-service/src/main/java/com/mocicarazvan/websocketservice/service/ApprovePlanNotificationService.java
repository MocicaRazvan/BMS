package com.mocicarazvan.websocketservice.service;


import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationBody;
import com.mocicarazvan.websocketservice.dtos.plan.ApprovePlanNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.models.Plan;
import com.mocicarazvan.websocketservice.service.generic.ApproveNotificationServiceTemplate;

public interface ApprovePlanNotificationService extends ApproveNotificationServiceTemplate<Plan, PlanResponse,
        ApprovePlanNotificationBody, ApprovePlanNotificationResponse
        > {
}

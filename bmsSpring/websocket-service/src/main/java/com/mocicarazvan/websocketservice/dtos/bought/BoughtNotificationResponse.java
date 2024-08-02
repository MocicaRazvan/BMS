package com.mocicarazvan.websocketservice.dtos.bought;

import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateResponse;
import com.mocicarazvan.websocketservice.dtos.plan.PlanResponse;
import com.mocicarazvan.websocketservice.enums.BoughtNotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
//@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BoughtNotificationResponse extends NotificationTemplateResponse<PlanResponse, BoughtNotificationType> {
}

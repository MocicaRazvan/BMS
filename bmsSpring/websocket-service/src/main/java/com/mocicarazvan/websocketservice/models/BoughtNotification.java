package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.enums.BoughtNotificationType;
import com.mocicarazvan.websocketservice.models.generic.NotificationTemplate;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@Entity
public class BoughtNotification extends NotificationTemplate<Plan, BoughtNotificationType> {
}

package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.enums.BoughtNotificationType;
import com.mocicarazvan.websocketservice.models.generic.NotificationTemplate;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(
        indexes = {
                @Index(name = "idx_sender_bought_id", columnList = "sender_id"),
                @Index(name = "idx_receiver_bought_id", columnList = "receiver_id"),
                @Index(name = "idx_reference_bought_id", columnList = "reference_id"),
        }
)
public class BoughtNotification extends NotificationTemplate<Plan, BoughtNotificationType> {
}

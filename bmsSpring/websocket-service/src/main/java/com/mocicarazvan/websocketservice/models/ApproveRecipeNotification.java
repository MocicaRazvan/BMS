package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
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
                @Index(name = "idx_sender_approve_recipe_id", columnList = "sender_id"),
                @Index(name = "idx_receiver_approve_recipe_id", columnList = "receiver_id"),
                @Index(name = "idx_reference_approve_recipe_id", columnList = "reference_id"),
        }
)
public class ApproveRecipeNotification extends NotificationTemplate<Recipe, ApprovedNotificationType> {
}

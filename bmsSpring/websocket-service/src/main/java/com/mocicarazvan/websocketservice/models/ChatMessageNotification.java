package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.enums.ChatMessageNotificationType;
import com.mocicarazvan.websocketservice.models.generic.NotificationTemplate;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
//@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        indexes = {
                @Index(name = "idx_sender_chat_message_notification_id", columnList = "sender_id"),
                @Index(name = "idx_receiver_chat_message_notification_id", columnList = "receiver_id"),
                @Index(name = "idx_reference_chat_message_notification_id", columnList = "reference_id"),
        }
)
public class ChatMessageNotification extends NotificationTemplate<ChatRoom, ChatMessageNotificationType> {

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ChatMessageNotification that = (ChatMessageNotification) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

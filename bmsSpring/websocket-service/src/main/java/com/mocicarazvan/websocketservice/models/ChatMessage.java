package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.models.generic.EncryptedContent;
import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        indexes = {
                @Index(name = "idx_sender_chat_message_id", columnList = "sender_id"),
                @Index(name = "idx_receiver_chat_message_id", columnList = "receiver_id"),
                @Index(name = "idx_chat_room_chat_message_id", columnList = "chat_room_id"),
                @Index(name = "idx_sender_id_receiver_id_chat_room_id_chat_message", columnList = "sender_id, receiver_id, chat_room_id")
        }
)
public class ChatMessage extends EncryptedContent implements Transformable<ChatMessage> {


    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sender_id", nullable = false)
    private ConversationUser sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "receiver_id", nullable = false)
    private ConversationUser receiver;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;


    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ChatMessage that = (ChatMessage) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

package com.mocicarazvan.websocketservice.models.generic;

import com.mocicarazvan.websocketservice.models.ConversationUser;
import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class NotificationTemplate<R extends IdGenerated, E extends Enum<E>> extends IdGenerated implements Transformable<NotificationTemplate<R, E>> {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private ConversationUser sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private ConversationUser receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private E type;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "reference_id", nullable = false)
    private R reference;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String extraLink;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Version
    private Long version;

    @PrePersist
    protected void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}

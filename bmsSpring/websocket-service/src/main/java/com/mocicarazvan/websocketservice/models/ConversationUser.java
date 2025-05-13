package com.mocicarazvan.websocketservice.models;


import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import com.mocicarazvan.websocketservice.models.generic.IdGenerated;
import com.mocicarazvan.websocketservice.utils.EmailNormalizerWrapperHolder;
import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class ConversationUser extends IdGenerated implements Transformable<ConversationUser> {

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectedStatus connectedStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "connected_chat_room_id")
    private ChatRoom connectedChatRoom;

    @Version
    private Long version;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        this.email = EmailNormalizerWrapperHolder.EmailNormalizer.normalize(email);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ConversationUser that = (ConversationUser) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

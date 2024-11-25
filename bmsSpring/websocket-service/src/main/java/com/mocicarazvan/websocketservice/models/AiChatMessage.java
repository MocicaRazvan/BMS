package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.convertors.AiChatRoleConvertor;
import com.mocicarazvan.websocketservice.enums.AiChatRole;
import com.mocicarazvan.websocketservice.models.generic.IdGenerated;
import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        indexes = {
                @Index(name = "idx_chat_ai_chat_message_room_id", columnList = "chat_room_id")
        }
)
public class AiChatMessage extends IdGenerated implements Transformable<AiChatMessage> {

    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(columnDefinition = "TEXT")
    private String vercelId;

    @Convert(converter = AiChatRoleConvertor.class)
    private AiChatRole role;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AiChatRoom chatRoom;
}

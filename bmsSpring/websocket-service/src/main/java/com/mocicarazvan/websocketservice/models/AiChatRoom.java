package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.models.generic.IdGenerated;
import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        indexes = {
                @Index(name = "idx_user_ai_chat_room_id", columnList = "user_id")
        }
)
public class AiChatRoom extends IdGenerated implements Transformable<AiChatRoom> {

    @OneToOne(fetch = FetchType.EAGER)
    private ConversationUser user;
}

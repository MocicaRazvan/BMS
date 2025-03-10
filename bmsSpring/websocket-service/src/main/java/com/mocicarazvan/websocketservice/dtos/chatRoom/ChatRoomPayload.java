package com.mocicarazvan.websocketservice.dtos.chatRoom;

import com.mocicarazvan.websocketservice.dtos.user.ConversationUserBase;
import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatRoomPayload implements Transformable<ChatRoomPayload> {
    @NotEmpty(message = "Users are required")
    private Set<ConversationUserBase> users;

}

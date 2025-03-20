package com.mocicarazvan.websocketservice.dtos.chatRoom;

import com.mocicarazvan.websocketservice.dtos.generic.IdResponse;
import com.mocicarazvan.websocketservice.dtos.user.JoinedConversationUser;
import com.mocicarazvan.websocketservice.utils.Transformable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatRoomResponseJoined extends IdResponse implements Transformable<ChatRoomResponseJoined> {

    private Set<JoinedConversationUser> users;

    public ChatRoomResponseJoined(ChatRoomResponse roomResponse, Set<JoinedConversationUser> users) {
        super(roomResponse.getId(), roomResponse.getCreatedAt(), roomResponse.getUpdatedAt());
        this.users = users;
    }
}

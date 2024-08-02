package com.mocicarazvan.websocketservice.dtos.user;

import com.mocicarazvan.websocketservice.dtos.chatRoom.ChatRoomResponse;
import com.mocicarazvan.websocketservice.utils.Transformable;
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
public class ConversationUserResponse extends ConversationUserBase implements Transformable<ConversationUserResponse> {
    private Long id;
    private ChatRoomResponse connectedChatRoom;
}

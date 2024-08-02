package com.mocicarazvan.websocketservice.dtos.user;

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
public class ConversationUserPayload extends ConversationUserBase
        implements Transformable<ConversationUserPayload> {
    private Long connectedChatRoomId;
}

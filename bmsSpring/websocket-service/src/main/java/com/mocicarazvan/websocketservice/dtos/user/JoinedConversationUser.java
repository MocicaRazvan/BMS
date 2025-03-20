package com.mocicarazvan.websocketservice.dtos.user;

import com.mocicarazvan.websocketservice.dtos.user.reactive.ReactiveUserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinedConversationUser {
    private ConversationUserResponse conversationUser;
    private ReactiveUserDto reactiveUser;
}

package com.mocicarazvan.websocketservice.dtos.chatRoom;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DeleteChatRoomRequest {
    @NotNull(message = "Chat room id is required")
    private Long chatRoomId;
    @NotEmpty(message = "Sender email is required")
    private String senderEmail;
}

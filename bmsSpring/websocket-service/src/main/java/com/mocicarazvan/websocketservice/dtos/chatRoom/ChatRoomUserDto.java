package com.mocicarazvan.websocketservice.dtos.chatRoom;

import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class ChatRoomUserDto implements Transformable<ChatRoomUserDto> {
    private Long chatId;
    @NotBlank(message = "User email is required")
    private String userEmail;

    public ChatRoomUserDto(Long chatId, String userEmail) {
        this.chatId = chatId;
        this.userEmail = userEmail;
    }
}

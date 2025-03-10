package com.mocicarazvan.websocketservice.dtos.message;

import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatMessagePayload implements Transformable<ChatMessagePayload> {

    @NotBlank(message = "Sender email is required")
    private String senderEmail;
    @NotBlank(message = "Receiver email is required")
    private String receiverEmail;
    @NotNull(message = "Chat room id is required")
    private Long chatRoomId;
    private String content;
}

package com.mocicarazvan.websocketservice.dtos.ai;

import com.mocicarazvan.websocketservice.enums.AiChatRole;
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
public class AiChatMessagePayload implements Transformable<AiChatMessagePayload> {
    private String content;
    @NotBlank(message = "Vercel id is required")
    private String vercelId;
    @NotNull(message = "Role is required")
    private AiChatRole role;
    @NotBlank(message = "Email is required")
    private String email;
}

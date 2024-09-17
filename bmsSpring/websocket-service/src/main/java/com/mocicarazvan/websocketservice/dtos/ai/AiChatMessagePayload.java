package com.mocicarazvan.websocketservice.dtos.ai;

import com.mocicarazvan.websocketservice.enums.AiChatRole;
import com.mocicarazvan.websocketservice.utils.Transformable;
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
    private String vercelId;
    private AiChatRole role;
    private String email;
}

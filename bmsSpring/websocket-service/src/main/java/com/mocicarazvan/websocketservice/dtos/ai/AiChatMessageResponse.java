package com.mocicarazvan.websocketservice.dtos.ai;

import com.mocicarazvan.websocketservice.dtos.generic.IdResponse;
import com.mocicarazvan.websocketservice.enums.AiChatRole;
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
public class AiChatMessageResponse
        extends IdResponse
        implements Transformable<AiChatMessageResponse> {
    private Long id;
    private String content;
    private String vercelId;
    private AiChatRole role;
}

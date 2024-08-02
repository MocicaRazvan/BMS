package com.mocicarazvan.websocketservice.dtos.generic;

import com.mocicarazvan.websocketservice.dtos.user.ConversationUserResponse;
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
public abstract class ApproveResponse extends IdResponse {
    private boolean approved;
    private ConversationUserResponse receiver;
    private long appId;
}

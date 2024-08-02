package com.mocicarazvan.websocketservice.dtos.generic;

import com.mocicarazvan.websocketservice.dtos.user.ConversationUserResponse;
import com.mocicarazvan.websocketservice.utils.Transformable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class NotificationTemplateResponse<R extends IdResponse, E extends Enum<E>> extends IdResponse
        implements Transformable<NotificationTemplateResponse<R, E>> {

    private ConversationUserResponse sender;
    private ConversationUserResponse receiver;
    private E type;
    private R reference;
    private String content;
    private String extraLink;
    private LocalDateTime timestamp;
}

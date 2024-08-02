package com.mocicarazvan.websocketservice.dtos.generic;

import com.mocicarazvan.websocketservice.utils.Transformable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class NotificationTemplateBody<E extends Enum<E>> implements Transformable<NotificationTemplateBody<E>> {
    private String senderEmail;
    private String receiverEmail;
    private E type;
    private Long referenceId;
    private String content;
    private String extraLink;
}

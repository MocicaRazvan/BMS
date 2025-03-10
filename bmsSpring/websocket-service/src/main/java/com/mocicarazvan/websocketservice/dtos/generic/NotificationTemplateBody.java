package com.mocicarazvan.websocketservice.dtos.generic;

import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class NotificationTemplateBody<E extends Enum<E>> implements Transformable<NotificationTemplateBody<E>> {
    @NotEmpty(message = "Sender email is required")
    private String senderEmail;
    @NotEmpty(message = "Receiver email is required")
    private String receiverEmail;
    @NotNull(message = "Type is required")
    private E type;
    private Long referenceId;
    private String content;
    private String extraLink;
}

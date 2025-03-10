package com.mocicarazvan.websocketservice.dtos.notifications;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SenderTypeDto<E extends Enum<E>> {
    @NotBlank(message = "Sender email is required")
    private String senderEmail;
    private E type;
}

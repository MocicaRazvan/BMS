package com.mocicarazvan.websocketservice.dtos.notifications;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SenderTypeDto<E extends Enum<E>> {
    @NotEmpty
    private String senderEmail;
    private E type;
}

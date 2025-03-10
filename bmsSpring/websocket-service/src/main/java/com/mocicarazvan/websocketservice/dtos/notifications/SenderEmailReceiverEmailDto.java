package com.mocicarazvan.websocketservice.dtos.notifications;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SenderEmailReceiverEmailDto {
    @NotBlank(message = "Sender email is required")
    private String senderEmail;
    @NotBlank(message = "Receiver email is required")
    private String receiverEmail;
}

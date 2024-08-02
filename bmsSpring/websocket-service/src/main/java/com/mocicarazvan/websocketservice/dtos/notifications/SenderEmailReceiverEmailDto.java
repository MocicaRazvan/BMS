package com.mocicarazvan.websocketservice.dtos.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SenderEmailReceiverEmailDto {
    private String senderEmail;
    private String receiverEmail;
}

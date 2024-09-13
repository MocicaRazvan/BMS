package com.mocicarazvan.websocketservice.messaging;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomConvertAndSendToUser {

    private final SimpMessagingTemplate messagingTemplate;

    // todo   user/{email}/queue/chat/{chatId} -> /queue/chat-{chatId}-{email} mereu email e la final
    public void sendToUser(String userEmail, String destination, Object payload) {
        messagingTemplate.convertAndSend(destination + "-" + userEmail, payload);
    }
}

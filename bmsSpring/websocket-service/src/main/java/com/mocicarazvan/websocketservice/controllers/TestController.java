package com.mocicarazvan.websocketservice.controllers;

import com.mocicarazvan.websocketservice.messaging.CustomConvertAndSendToUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final SimpMessagingTemplate messagingTemplate;
    private final CustomConvertAndSendToUser customConvertAndSendToUser;

    // todo   user/{email}/queue/chat/{chatId} -> /queue/chat-{chatId}-{email} mereu email e la final
    @GetMapping("/ws-http/test")
    public String test() {
        String userEmail = "razvanmocica1@gmail.com";  // Specific user
        String destination = "/queue/messages";        // Destination queue
        String message = "Hello, this is a test message!"; // Message content
        String sanitizedEmail = userEmail.replace("@", "-").replace(".", "-"); // Sanitize email


        // Send message to a specific user
//        messagingTemplate.convertAndSend(destination + "-" + userEmail, message);

        customConvertAndSendToUser.sendToUser(userEmail, destination, message);
        return "Message sent!";
    }
}

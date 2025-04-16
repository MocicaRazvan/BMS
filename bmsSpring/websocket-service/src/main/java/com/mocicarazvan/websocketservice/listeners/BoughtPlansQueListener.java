package com.mocicarazvan.websocketservice.listeners;

import com.mocicarazvan.websocketservice.dtos.bought.InternalBoughtBody;
import com.mocicarazvan.websocketservice.service.BoughtNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoughtPlansQueListener {
    private final BoughtNotificationService boughtNotificationService;

    @RabbitListener(queues = "#{@environment['plan.bought.queue.name']}")
    public void listen(@Valid @Payload InternalBoughtBody internalBoughtBody) {
        boughtNotificationService.saveInternalNotifications(internalBoughtBody);
    }
}

package com.mocicarazvan.templatemodule.services.impl;

import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@RequiredArgsConstructor
@Builder
public class RabbitMqSenderImpl implements RabbitMqSender {

    private final String exchangeName;
    private final String routingKey;
    private final RabbitTemplate rabbitTemplate;

    public <T> void sendMessage(T message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (exchangeName == null || exchangeName.isEmpty()) {
            throw new IllegalArgumentException("Exchange name cannot be null or empty");
        }
        if (routingKey == null || routingKey.isEmpty()) {
            throw new IllegalArgumentException("Routing key cannot be null or empty");
        }
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }
}

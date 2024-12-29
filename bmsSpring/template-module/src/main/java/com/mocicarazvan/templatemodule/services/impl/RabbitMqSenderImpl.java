package com.mocicarazvan.templatemodule.services.impl;

import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

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
        checkArgs();
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }

    private void checkArgs() {
        if (exchangeName == null || exchangeName.isEmpty()) {
            throw new IllegalArgumentException("Exchange name cannot be null or empty");
        }
        if (routingKey == null || routingKey.isEmpty()) {
            throw new IllegalArgumentException("Routing key cannot be null or empty");
        }
    }

    @Override
    public <T> void sendBatchMessage(List<T> message) {
        checkArgs();
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        message.parallelStream().forEach(m -> rabbitTemplate.convertAndSend(exchangeName, routingKey, m));
    }

}

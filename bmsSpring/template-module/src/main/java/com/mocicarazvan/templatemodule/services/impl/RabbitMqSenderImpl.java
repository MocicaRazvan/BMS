package com.mocicarazvan.templatemodule.services.impl;

import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.utils.MonoWrapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

@RequiredArgsConstructor
@Builder
@Slf4j
public class RabbitMqSenderImpl implements RabbitMqSender {

    private final String exchangeName;
    private final String routingKey;
    private final RabbitTemplate rabbitTemplate;

    public <T> void sendMessage(T message) {
        checkArgs(List.of(message));
        MonoWrapper.wrapBlockingFunction(() -> rabbitTemplate.convertAndSend(exchangeName, routingKey, message));
    }

    private <T> void checkArgs(List<T> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (exchangeName == null || exchangeName.isEmpty()) {
            throw new IllegalArgumentException("Exchange name cannot be null or empty");
        }
        if (routingKey == null || routingKey.isEmpty()) {
            throw new IllegalArgumentException("Routing key cannot be null or empty");
        }
    }

    @Override
    public <T> void sendBatchMessage(List<T> messages) {
        checkArgs(messages);
        MonoWrapper.wrapBlockingFunction(() -> rabbitTemplate.invoke(rabbitOperations -> {
            // todo test may cause issues mixing reactor and parallelStream
            messages.parallelStream()
                    .forEach(m -> rabbitOperations.convertAndSend(exchangeName, routingKey, m));
            return true;
        }));
    }

}

package com.mocicarazvan.templatemodule.testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RabbitTestUtils {
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public <T> List<T> drainTestQueue(String queueName, ParameterizedTypeReference<T> typeReference, long timeout, int expectCount) {
        List<T> messages = new ArrayList<>();
        int maxCount = expectCount + 1;
        for (int i = 0; i < maxCount; i++) {
            T message = rabbitTemplate.receiveAndConvert(queueName, timeout, typeReference);
            if (message != null) {
                messages.add(message);
            } else {
                break;
            }
        }
        return messages;

    }

    @SuppressWarnings("unchecked")
    public <T> List<Pair<T, Map<String, Object>>> drainTestQueueWithHeaders(String queueName, ParameterizedTypeReference<T> typeReference, long timeout, int expectCount) {
        List<Pair<T, Map<String, Object>>> result = new ArrayList<>();
        int maxCount = expectCount + 1;
        MessageConverter converter = rabbitTemplate.getMessageConverter();
        for (int i = 0; i < maxCount; i++) {
            Message raw = rabbitTemplate.receive(queueName, timeout);
            if (raw != null) {
                T message = (T) converter.fromMessage(raw);
                Map<String, Object> headers = raw.getMessageProperties().getHeaders();
                result.add(Pair.of(message, headers));
            } else {
                break;
            }
        }
        return result;

    }

    public <T> boolean verifyQueueIsEmpty(String queueName, ParameterizedTypeReference<T> typeReference, long timeout) {
        return drainTestQueue(queueName, typeReference, timeout, 0).isEmpty();
    }


}

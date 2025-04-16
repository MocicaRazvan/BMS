package com.mocicarazvan.websocketservice.utils;

import org.springframework.amqp.core.Queue;

import java.util.HashMap;
import java.util.Map;

public class RabbitMqQueueUtils {
    public static Queue durableQueueWithDlq(String queueName) {
        if (queueName == null || queueName.isBlank()) {
            throw new IllegalArgumentException("Queue name cannot be null or empty");
        }
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "");
        args.put("x-dead-letter-routing-key", queueName + ".dlq");
        return new Queue(queueName, true, false, false, args);
    }

    public static Queue deadLetterQueue(String queueName) {
        if (queueName == null || queueName.isBlank()) {
            throw new IllegalArgumentException("Queue name cannot be null or empty");
        }
        return new Queue(queueName + ".dlq", true);
    }
}

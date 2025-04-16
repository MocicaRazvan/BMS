package com.mocicarazvan.websocketservice.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.amqp.core.Queue;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMqQueueUtilsTest {
    @Test
    void durableQueueWithDlq_createsQueueWithCorrectNameAndArguments() {
        String queueName = "testQueue";
        Queue queue = RabbitMqQueueUtils.durableQueueWithDlq(queueName);

        assertNotNull(queue);
        assertEquals(queueName, queue.getName());
        assertTrue(queue.isDurable());
        assertFalse(queue.isExclusive());
        assertFalse(queue.isAutoDelete());
        assertEquals("", queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals(queueName + ".dlq", queue.getArguments().get("x-dead-letter-routing-key"));
    }


    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n"})
    @NullAndEmptySource
    void durableQueueWithDlq_throwsException(String queueName) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RabbitMqQueueUtils.durableQueueWithDlq(queueName)
        );
        assertEquals("Queue name cannot be null or empty", exception.getMessage());
    }

    @Test
    void deadLetterQueue_createsQueueWithCorrectName() {
        String queueName = "testQueue";
        Queue queue = RabbitMqQueueUtils.deadLetterQueue(queueName);

        assertNotNull(queue);
        assertEquals(queueName + ".dlq", queue.getName());
        assertTrue(queue.isDurable());
        assertFalse(queue.isExclusive());
        assertFalse(queue.isAutoDelete());
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n"})
    @NullAndEmptySource
    void deadLetterQueue_throwsException(String queueName) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                RabbitMqQueueUtils.deadLetterQueue(queueName)
        );
        assertEquals("Queue name cannot be null or empty", exception.getMessage());
    }


}
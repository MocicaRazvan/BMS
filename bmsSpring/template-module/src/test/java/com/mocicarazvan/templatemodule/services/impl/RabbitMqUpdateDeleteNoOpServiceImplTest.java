package com.mocicarazvan.templatemodule.services.impl;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RabbitMqUpdateDeleteNoOpServiceImplTest {

    private final RabbitMqUpdateDeleteNoOpServiceImpl<Object> service = new RabbitMqUpdateDeleteNoOpServiceImpl<>();

    @Test
    void sendUpdateMessage_doesNotThrowException() {
        Object model = new Object();
        assertDoesNotThrow(() -> service.sendUpdateMessage(model));
    }

    @Test
    void sendDeleteMessage_doesNotThrowException() {
        Object model = new Object();
        assertDoesNotThrow(() -> service.sendDeleteMessage(model));
    }

    @Test
    void sendBatchUpdateMessage_withEmptyList_doesNotThrowException() {
        List<Object> models = Collections.emptyList();
        assertDoesNotThrow(() -> service.sendBatchUpdateMessage(models));
    }

    @Test
    void sendBatchUpdateMessage_withNonEmptyList_doesNotThrowException() {
        List<Object> models = List.of(new Object(), new Object());
        assertDoesNotThrow(() -> service.sendBatchUpdateMessage(models));
    }

    @Test
    void sendBatchDeleteMessage_withEmptyList_doesNotThrowException() {
        List<Object> models = Collections.emptyList();
        assertDoesNotThrow(() -> service.sendBatchDeleteMessage(models));
    }

    @Test
    void sendBatchDeleteMessage_withNonEmptyList_doesNotThrowException() {
        List<Object> models = List.of(new Object(), new Object());
        assertDoesNotThrow(() -> service.sendBatchDeleteMessage(models));
    }
}
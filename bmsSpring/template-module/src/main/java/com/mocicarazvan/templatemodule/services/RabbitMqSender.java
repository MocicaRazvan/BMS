package com.mocicarazvan.templatemodule.services;

import java.util.List;

public interface RabbitMqSender {

    <T> void sendMessage(T message);

    <T> void sendBatchMessage(List<T> message);
}

package com.mocicarazvan.templatemodule.services;

import reactor.util.retry.RetryBackoffSpec;

import java.util.List;
import java.util.Map;

public interface RabbitMqSender {

    <T> void sendMessage(T message);

    <T> void sendBatchMessage(List<T> message);

    RetryBackoffSpec getRetrySpec();

    <T> void sendMessageWithHeaders(T message, Map<String, Object> headers);

    void configureRetry(int retryCount, int retryDelaySeconds);
}

package com.mocicarazvan.templatemodule.services;

import reactor.util.retry.RetryBackoffSpec;

import java.util.List;

public interface RabbitMqSender {

    <T> void sendMessage(T message);

    <T> void sendBatchMessage(List<T> message);

    RetryBackoffSpec getRetrySpec();

    void configureRetry(int retryCount, int retryDelaySeconds);
}

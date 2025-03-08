package com.mocicarazvan.templatemodule.services.impl;

import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.utils.MonoWrapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Builder
@Slf4j
@Setter
@Getter
@AllArgsConstructor
public class RabbitMqSenderImpl implements RabbitMqSender {


    private final String exchangeName;
    private final String routingKey;
    private final RabbitTemplate rabbitTemplate;
    private final int concurrency;
    @Builder.Default
    private int retryCount = 3;
    @Builder.Default
    private int retryDelaySeconds = 2;

    public <T> void sendMessage(T message) {
        checkArgs(List.of(message));
//        log.info("Sending message: {} in exchange: {} with routing key: {}", message, exchangeName, routingKey);
        MonoWrapper.wrapBlockingFunction(() -> rabbitTemplate.convertAndSend(exchangeName, routingKey, message), getRetrySpec());
    }

    private <T> void checkArgs(List<T> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (exchangeName == null || exchangeName.isEmpty()) {
            throw new IllegalArgumentException("Exchange name cannot be null or empty");
        }
        if (routingKey == null) {
            throw new IllegalArgumentException("Routing key cannot be null");
        }
    }

    @Override
    public <T> void sendBatchMessage(List<T> messages) {
        checkArgs(messages);

        Flux.fromIterable(messages)
                .flatMap(m -> Mono.fromRunnable(() -> rabbitTemplate.convertAndSend(exchangeName, routingKey, m))
                        .retryWhen(getRetrySpec())
                        .subscribeOn(Schedulers.boundedElastic()), concurrency)
                .doOnError(e -> log.error("Error sending message to rabbitmq: {}", e.getMessage()))
                .subscribe();
    }

    @Override
    public RetryBackoffSpec getRetrySpec() {
        return Retry.backoff(retryCount, Duration.ofSeconds(retryDelaySeconds))
                .doBeforeRetry(retrySignal ->
                        log.warn("Retrying message send attempt {} due to: {}", retrySignal.totalRetriesInARow(), retrySignal.failure().getMessage()));
    }

    @Override
    public void configureRetry(int retryCount, int retryDelaySeconds) {
        this.retryCount = retryCount;
        this.retryDelaySeconds = retryDelaySeconds;
    }

}

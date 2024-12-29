package com.mocicarazvan.archiveservice.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.archiveservice.config.QueuesPropertiesConfig;
import com.mocicarazvan.archiveservice.services.DirService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
// todo implement this well its just a placeholder
public class ChannelAwareBatchMessageListenerImpl<T> implements ChannelAwareBatchMessageListener {

    private final ObjectMapper objectMapper;
    private final Class<T> clazz;
    private final String queueName;
    private final DirService dirService;
    private final QueuesPropertiesConfig queuesPropertiesConfig;


    @Override
    public void onMessageBatch(List<Message> messages, Channel channel) {
        log.info("Processing for class: " + clazz.getName());
        log.info("Processing batch of messages: " + messages.size());
        try {
            Flux.fromIterable(messages)
                    .map(this::deserializeMessage)
                    .bufferTimeout(queuesPropertiesConfig.getBatchSize(), Duration.ofSeconds(queuesPropertiesConfig.getSavingBufferSeconds()))
                    .flatMap(this::saveBatchToDisk)
                    .doOnError(e -> {
                        log.error("Error during processing: {}", e.getMessage());
                        rejectBatch(messages, channel);
                    })
                    .doOnComplete(() -> acknowledgeBatch(messages, channel))
                    .subscribe();
        } catch (Exception e) {
            log.error("Error processing batch of messages: " + e.getMessage());
            rejectBatch(messages, channel);
        }
    }

    private static void rejectBatch(List<Message> messages, Channel channel) {
        messages.forEach(message -> {
            try {
                long deliveryTag = message.getMessageProperties().getDeliveryTag();
                channel.basicReject(deliveryTag, true);
                log.info("Rejected message with delivery tag: " + deliveryTag);
            } catch (IOException ex) {
                log.error("Error rejecting message: " + ex.getMessage());
            }
        });
    }

    private Mono<Void> saveBatchToDisk(List<T> batch) {

        return Mono.fromCallable(() -> {
                    dirService.saveBatchToDisk(batch, queueName);
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(ignored -> log.info("Saved {} messages to disk for queue {}", batch.size(), queueName))
                .then();
    }

    private T deserializeMessage(Message message) {
        try {
            return objectMapper.readValue(message.getBody(), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing message: " + e.getMessage(), e);
        }
    }

    private void acknowledgeBatch(List<Message> messages, Channel channel) {
        try {
            long lastDeliveryTag = messages.getLast().getMessageProperties().getDeliveryTag();
            channel.basicAck(lastDeliveryTag, true);
            log.info("Acknowledged messages up to {}", lastDeliveryTag);
        } catch (Exception e) {
            log.error("Error acknowledging messages: {}", e.getMessage());
        }
    }
}

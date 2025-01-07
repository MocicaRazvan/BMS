package com.mocicarazvan.archiveservice.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.archiveservice.config.QueuesPropertiesConfig;
import com.mocicarazvan.archiveservice.services.SaveMessagesAggregator;
import com.mocicarazvan.archiveservice.utils.MonoWrapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Slf4j
public class ChannelAwareBatchMessageListenerImpl<T> implements ChannelAwareBatchMessageListener {

    private final ObjectMapper objectMapper;
    private final Class<T> clazz;
    private final String queueName;
    private final SaveMessagesAggregator saveMessagesAggregator;
    private final QueuesPropertiesConfig queuesPropertiesConfig;
    private final Scheduler scheduler;

    public ChannelAwareBatchMessageListenerImpl(ObjectMapper objectMapper,
                                                Class<T> clazz, String queueName, SaveMessagesAggregator saveMessagesAggregator,
                                                QueuesPropertiesConfig queuesPropertiesConfig,
                                                ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.objectMapper = objectMapper;
        this.clazz = clazz;
        this.queueName = queueName;
        this.saveMessagesAggregator = saveMessagesAggregator;
        this.queuesPropertiesConfig = queuesPropertiesConfig;
        this.scheduler = Schedulers.fromExecutor(threadPoolTaskScheduler);
    }


    @Override
    public void onMessageBatch(List<Message> messages, Channel channel) {
//        log.info("Processing for class: {}", clazz.getName());
//        log.info("Processing batch of messages: {}", messages.size());
//        log.info("Thread name is {} and virtual thread is {}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        try {
            Flux.fromIterable(messages)
                    .flatMap(m -> Mono.fromCallable(() -> deserializeMessage(m)
                            ).subscribeOn(scheduler)
                    )
                    .bufferTimeout(queuesPropertiesConfig.getBatchSize(), Duration.ofSeconds(queuesPropertiesConfig.getSavingBufferSeconds()))
                    .flatMap(this::sendBatchToBeSaved)
                    .doOnError(e -> {
                        log.error("Error during processing: {}", e.getMessage());
                        rejectBatch(messages, channel);
                    })
                    .doOnComplete(() ->
                    {
                        acknowledgeBatch(messages, channel);
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Error processing batch of messages: {}", e.getMessage());
            rejectBatch(messages, channel);
        }
    }

    private static void rejectBatch(List<Message> messages, Channel channel) {
        messages.forEach(message -> {
            try {
                long deliveryTag = message.getMessageProperties().getDeliveryTag();
                channel.basicReject(deliveryTag, true);
//                log.info("Rejected message with delivery tag: {}", deliveryTag);
            } catch (IOException ex) {
                log.error("Error rejecting message: {}", ex.getMessage());
            }
        });
    }

    private Mono<Void> sendBatchToBeSaved(List<T> batch) {

        return
                Mono.fromCallable(() -> {
                            saveMessagesAggregator.getSaveBatchMessages().saveBatch(batch, queueName);
                            return null;
                        })
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
        MonoWrapper.wrapBlockingFunction(() -> {
            try {
                long lastDeliveryTag = messages.getLast().getMessageProperties().getDeliveryTag();
                channel.basicAck(lastDeliveryTag, true);
                saveMessagesAggregator.getBatchNotify().notifyBatchUpdate(messages, queueName);
//            log.info("Acknowledged messages up to {}", lastDeliveryTag);
            } catch (Exception e) {
                log.error("Error acknowledging messages: {}", e.getMessage());
            }
        });
    }
}

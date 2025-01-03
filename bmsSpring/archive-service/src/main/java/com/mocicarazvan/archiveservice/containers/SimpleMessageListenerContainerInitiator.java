package com.mocicarazvan.archiveservice.containers;

import com.mocicarazvan.archiveservice.config.QueuesPropertiesConfig;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;


@RequiredArgsConstructor
@Builder
public class SimpleMessageListenerContainerInitiator {
    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final SimpleAsyncTaskScheduler executor;
    private final QueuesPropertiesConfig queuesPropertiesConfig;
    private final ChannelAwareBatchMessageListener channelAwareBatchMessageListener;

    public SimpleMessageListenerContainer initContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setConsumerBatchEnabled(true);
        container.setBatchSize(queuesPropertiesConfig.getBatchSize());
        container.setPrefetchCount(queuesPropertiesConfig.getBatchSize());
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setStartConsumerMinInterval(15000);
        container.setConsumerStartTimeout(150000);
        container.setShutdownTimeout(5000);
        container.setReceiveTimeout(2500);
        container.setConcurrency(queuesPropertiesConfig.getConcurrency());
        container.setTaskExecutor(executor);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setDeclarationRetries(3);
        container.setAlwaysRequeueWithTxManagerRollback(true);
        container.setMessageListener(channelAwareBatchMessageListener);
        return container;
    }


}

package com.mocicarazvan.archiveservice.containers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.archiveservice.config.QueuesPropertiesConfig;
import com.mocicarazvan.archiveservice.dtos.*;
import com.mocicarazvan.archiveservice.exceptions.QueueNameNotValid;
import com.mocicarazvan.archiveservice.exceptions.QueuePrefixNotFound;
import com.mocicarazvan.archiveservice.listeners.ChannelAwareBatchMessageListenerImpl;
import com.mocicarazvan.archiveservice.services.SaveMessagesAggregator;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SimpleMessageListenerContainerFactory {
    private final SimpleAsyncTaskScheduler executor;
    private final ConnectionFactory connectionFactory;
    private final QueuesPropertiesConfig queuesPropertiesConfig;
    private final ObjectMapper objectMapper;
    private final SaveMessagesAggregator saveMessagesAggregator;

    private static final List<String> queuePrefixes = List.of("user", "comment", "day", "ingredient", "meal", "plan", "post", "recipe", "nutritionalFact");
    private static final Map<String, Class<?>> queueToClassMap = Map.of(
            "user", UserCustom.class,
            "comment", Comment.class,
            "day", Day.class,
            "ingredient", Ingredient.class,
            "meal", Meal.class,
            "plan", Plan.class,
            "post", Post.class,
            "recipe", Recipe.class,
            "nutritionalFact", NutritionalFact.class
    );

    public SimpleMessageListenerContainerFactory(@Qualifier("containerLifecycleSimpleAsyncTaskScheduler") SimpleAsyncTaskScheduler executor, ConnectionFactory connectionFactory, QueuesPropertiesConfig queuesPropertiesConfig, ObjectMapper objectMapper, SaveMessagesAggregator saveMessagesAggregator) {
        this.executor = executor;
        this.connectionFactory = connectionFactory;
        this.queuesPropertiesConfig = queuesPropertiesConfig;
        this.objectMapper = objectMapper;
        this.saveMessagesAggregator = saveMessagesAggregator;
    }

    public SimpleMessageListenerContainer createContainer(String queueName) {
        if (queuesPropertiesConfig.getQueues().stream().noneMatch(queueName::equals)) {
            throw new QueueNameNotValid(queueName);
        }
        Class<?> clazz = getClassFromQueue(queueName);
        return SimpleMessageListenerContainerInitiator.builder()
                .connectionFactory(connectionFactory)
                .queueName(queueName)
                .executor(executor)
                .queuesPropertiesConfig(queuesPropertiesConfig)
                .channelAwareBatchMessageListener(new ChannelAwareBatchMessageListenerImpl<>(objectMapper, clazz, queueName, saveMessagesAggregator, queuesPropertiesConfig))
                .build()
                .initContainer();
    }

    public List<SimpleMessageListenerContainer> createAllContainers() {
        System.out.println(queuesPropertiesConfig.getQueuesJobs());
        return queuesPropertiesConfig.getQueues().stream()
                .map(this::createContainer)
                .toList();
    }


    public Class<?> getClassFromQueue(String queueName) {
        return queuePrefixes.stream().map(String::toLowerCase)
                .filter(queueName::startsWith)
                .findAny()
                .map(queueToClassMap::get)
                .orElseThrow(() -> new QueuePrefixNotFound(queueName));
    }
}

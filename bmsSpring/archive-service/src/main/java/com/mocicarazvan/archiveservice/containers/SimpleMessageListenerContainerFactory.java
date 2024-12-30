package com.mocicarazvan.archiveservice.containers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.archiveservice.config.QueuesPropertiesConfig;
import com.mocicarazvan.archiveservice.dtos.*;
import com.mocicarazvan.archiveservice.exceptions.QueueNameNotValid;
import com.mocicarazvan.archiveservice.exceptions.QueuePrefixNotFound;
import com.mocicarazvan.archiveservice.listeners.ChannelAwareBatchMessageListenerImpl;
import com.mocicarazvan.archiveservice.services.SaveBatchMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class SimpleMessageListenerContainerFactory {
    private final ThreadPoolTaskScheduler executor;
    private final ConnectionFactory connectionFactory;
    private final QueuesPropertiesConfig queuesPropertiesConfig;
    private final ObjectMapper objectMapper;
    private final SaveBatchMessages saveBatchMessages;

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
                .channelAwareBatchMessageListener(new ChannelAwareBatchMessageListenerImpl<>(objectMapper, clazz, queueName, saveBatchMessages, queuesPropertiesConfig))
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

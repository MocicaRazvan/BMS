package com.mocicarazvan.archiveservice.config;

import com.mocicarazvan.archiveservice.containers.ContainerScheduler;
import com.mocicarazvan.archiveservice.containers.SimpleMessageListenerContainerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig {

    private final SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory;
    private final GenericApplicationContext applicationContext;
    private final QueuesPropertiesConfig queuesPropertiesConfig;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

//    @Bean
//    public List<SimpleMessageListenerContainer> simpleMessageListenerContainers() {
//        return simpleMessageListenerContainerFactory.createAllContainers()
//                .stream().peek(
//                        container -> applicationContext.registerBean("rabbitListenerContainer" + Arrays.toString(container.getQueueNames()), SimpleMessageListenerContainer.class, () -> container)
//                ).toList();
//    }

    @Bean
    public List<ContainerScheduler> containerSchedulers() {
        return simpleMessageListenerContainerFactory.createAllContainers()
                .stream().map(
                        container -> new ContainerScheduler(container, threadPoolTaskScheduler, queuesPropertiesConfig)
                )
                .peek(
                        containerScheduler -> {
                            applicationContext.registerBean("containerScheduler" + Arrays.toString(containerScheduler.getSimpleMessageListenerContainer().getQueueNames()), ContainerScheduler.class, () -> containerScheduler);
                            containerScheduler.scheduleContainer();
                        }
                )
                .toList();
    }

}

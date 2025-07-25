package com.mocicarazvan.archiveservice.config.rabbit;

import com.mocicarazvan.archiveservice.containers.SimpleMessageListenerContainerFactory;
import com.mocicarazvan.archiveservice.schedulers.ContainerScheduler;
import com.mocicarazvan.archiveservice.services.ContainerNotify;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class RabbitMqConfig implements RabbitListenerConfigurer {

    private final SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory;
    private final GenericApplicationContext applicationContext;
    private final QueuesPropertiesConfig queuesPropertiesConfig;
    private final SimpleAsyncTaskScheduler simpleAsyncTaskScheduler;
    private final LocalValidatorFactoryBean validator;

    public RabbitMqConfig(SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory, GenericApplicationContext applicationContext, QueuesPropertiesConfig queuesPropertiesConfig, @Qualifier("containerSimpleAsyncTaskScheduler") SimpleAsyncTaskScheduler simpleAsyncTaskScheduler, LocalValidatorFactoryBean validator) {
        this.simpleMessageListenerContainerFactory = simpleMessageListenerContainerFactory;
        this.applicationContext = applicationContext;
        this.queuesPropertiesConfig = queuesPropertiesConfig;
        this.simpleAsyncTaskScheduler = simpleAsyncTaskScheduler;
        this.validator = validator;
    }


    @Bean
    public Map<String, ContainerScheduler> containerSchedulers(ContainerNotify containerNotify) {
        return simpleMessageListenerContainerFactory.createAllContainers()
                .stream()
                .collect(Collectors.toMap(
                        container -> container.getQueueNames()[0],
                        container -> {
                            ContainerScheduler containerScheduler = new ContainerScheduler(container, simpleAsyncTaskScheduler, queuesPropertiesConfig, containerNotify);

                            applicationContext.registerBean(
                                    "containerScheduler" + container.getQueueNames()[0],
                                    ContainerScheduler.class,
                                    () -> containerScheduler
                            );

                            containerScheduler.scheduleContainer();

                            return containerScheduler;
                        }
                ));
    }

    @Bean
    public RabbitAdmin admin(ConnectionFactory cf, @Qualifier("simpleAsyncTaskExecutor") SimpleAsyncTaskExecutor simpleAsyncTaskExecutor) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(cf);
        rabbitAdmin.setTaskExecutor(simpleAsyncTaskExecutor);
        rabbitAdmin.setRetryTemplate(RetryTemplate.defaultInstance());
        return rabbitAdmin;
    }


    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setValidator(validator);
    }
}

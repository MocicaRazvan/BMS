package com.mocicarazvan.websocketservice.config;

import com.mocicarazvan.websocketservice.utils.RabbitMqQueueUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@Slf4j
public class RabbitMqConfig implements RabbitListenerConfigurer {
    @Value("${plan.queue.name}")
    private String planQueueName;

    @Value("${post.queue.name}")
    private String postQueueName;

    @Value("${recipe.queue.name}")
    private String recipeQueueName;

    @Value("${plan.bought.queue.name}")
    private String planBoughtQueueName;


    private final SimpleAsyncTaskScheduler taskScheduler;

    private final SimpleAsyncTaskExecutor taskExecutor;

    private final LocalValidatorFactoryBean validator;


    public RabbitMqConfig(@Qualifier("simpleAsyncTaskScheduler") SimpleAsyncTaskScheduler taskScheduler,
                          @Qualifier("scheduledExecutorService") SimpleAsyncTaskExecutor taskExecutor,
                          LocalValidatorFactoryBean validator
    ) {
        this.taskScheduler = taskScheduler;
        this.taskExecutor = taskExecutor;
        this.validator = validator;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer
    ) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setTaskExecutor(taskExecutor);
        return factory;
    }

    //    @Bean
//    public Queue planQueue() {
//        return new Queue(planQueueName, true);
//    }
//
//    @Bean
//    public Queue postQueue() {
//        return new Queue(postQueueName, true);
//    }
//
//    @Bean
//    public Queue recipeQueue() {
//        return new Queue(recipeQueueName, true);
//    }
//
//    @Bean
//    public Queue planBoughtQueue() {
//        return new Queue(planBoughtQueueName, true);
//    }
    @Bean
    public Queue planQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(planQueueName);
    }

    @Bean
    public Queue planDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(planQueueName);
    }

    @Bean
    public Queue postQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(postQueueName);
    }

    @Bean
    public Queue postDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(postQueueName);
    }

    @Bean
    public Queue recipeQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(recipeQueueName);
    }

    @Bean
    public Queue recipeDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(recipeQueueName);
    }

    @Bean
    public Queue planBoughtQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(planBoughtQueueName);
    }

    @Bean
    public Queue planBoughtDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(planBoughtQueueName);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setTaskExecutor(taskScheduler);
        rabbitTemplate.setRetryTemplate(RetryTemplate.defaultInstance());
        return rabbitTemplate;
    }

    @Bean
    public AsyncRabbitTemplate asyncRabbitTemplate(final RabbitTemplate rabbitTemplate) {
        AsyncRabbitTemplate asyncRabbitTemplate = new AsyncRabbitTemplate(rabbitTemplate);
        asyncRabbitTemplate.setTaskScheduler(taskScheduler);
        return asyncRabbitTemplate;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setValidator(validator);
    }


}

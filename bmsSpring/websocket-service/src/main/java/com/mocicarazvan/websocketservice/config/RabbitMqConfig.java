package com.mocicarazvan.websocketservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

@Configuration
public class RabbitMqConfig {
    @Value("${plan.queue.name}")
    private String planQueueName;

    @Value("${post.queue.name}")
    private String postQueueName;

    @Value("${recipe.queue.name}")
    private String recipeQueueName;

    @Value("${plan.bought.queue.name}")
    private String planBoughtQueueName;


    private final SimpleAsyncTaskScheduler taskScheduler;

    public RabbitMqConfig(@Qualifier("simpleAsyncTaskScheduler") SimpleAsyncTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @Bean
    public Queue planQueue() {
        return new Queue(planQueueName, true);
    }

    @Bean
    public Queue postQueue() {
        return new Queue(postQueueName, true);
    }

    @Bean
    public Queue recipeQueue() {
        return new Queue(recipeQueueName, true);
    }

    @Bean
    public Queue planBoughtQueue() {
        return new Queue(planBoughtQueueName, true);
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
}

package com.mocicarazvan.websocketservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return rabbitTemplate;
    }
}

package com.mocicarazvan.orderservice.config;

import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
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
    @Value("${plan.bought.queue.name}")
    private String planBoughtQueueName;

    @Value("${plan.bought.exchange.name}")
    private String planBoughtExchangeName;

    @Value("${plan.bought.routing.key}")
    private String planBoughtRoutingKey;

    @Bean
    public Queue planBoughtQueue() {
        return new Queue(planBoughtQueueName, true);
    }

    @Bean
    public DirectExchange planBoughtExchange() {
        return new DirectExchange(planBoughtExchangeName);
    }

    @Bean
    public Binding planBoughtBinding(Queue planBoughtQueue, DirectExchange planBoughtExchange) {
        return BindingBuilder.bind(planBoughtQueue).to(planBoughtExchange).with(planBoughtRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public RabbitMqSender rabbitMqSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(planBoughtExchangeName, planBoughtRoutingKey, rabbitTemplate);
    }

}

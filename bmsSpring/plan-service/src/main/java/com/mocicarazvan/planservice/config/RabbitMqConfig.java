package com.mocicarazvan.planservice.config;

import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSenderWrapper;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqApprovedSenderWrapperImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
public class RabbitMqConfig {
    @Value("${plan.queue.name}")
    private String planQueueName;

    @Value("${plan.exchange.name}")
    private String planExchangeName;

    @Value("${plan.routing.key}")
    private String planRoutingKey;

    @Value("${plan.extraLink}")
    private String extraLink;

    @Bean
    public Queue planQueue() {
        return new Queue(planQueueName, true);
    }

    @Bean
    public DirectExchange planExchange() {
        return new DirectExchange(planExchangeName);
    }

    @Bean
    public Binding planBinding(Queue planQueue, DirectExchange planExchange) {
        return BindingBuilder.bind(planQueue).to(planExchange).with(planRoutingKey);
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
        return new RabbitMqSenderImpl(planExchangeName, planRoutingKey, rabbitTemplate);
    }

    @Bean
    public RabbitMqApprovedSenderWrapper<PlanResponse> planResponseRabbitMqApprovedSenderWrapper(RabbitMqSender rabbitMqSender) {
        return new RabbitMqApprovedSenderWrapperImpl<>(extraLink, rabbitMqSender);
    }
}

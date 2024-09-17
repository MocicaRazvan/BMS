package com.mocicarazvan.postservice.conifg;

import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSenderWrapper;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqApprovedSenderWrapperImpl;
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
    @Value("${post.queue.name}")
    private String postQueueName;

    @Value("${post.exchange.name}")
    private String postExchangeName;

    @Value("${post.routing.key}")
    private String postRoutingKey;

    @Value("${post.extraLink}")
    private String extraLink;

    @Bean
    public Queue postQueue() {
        return new Queue(postQueueName, true);
    }

    @Bean
    public DirectExchange postExchange() {
        return new DirectExchange(postExchangeName);
    }

    @Bean
    public Binding postBinding(Queue postQueue, DirectExchange postExchange) {
        return BindingBuilder.bind(postQueue).to(postExchange).with(postRoutingKey);
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
        return new RabbitMqSenderImpl(postExchangeName, postRoutingKey, rabbitTemplate);
    }

    @Bean
    public RabbitMqApprovedSenderWrapper<PostResponse> postResponseRabbitMqApprovedSenderWrapper(RabbitMqSender rabbitMqSender) {
        return new RabbitMqApprovedSenderWrapperImpl<>(extraLink, rabbitMqSender);
    }
}

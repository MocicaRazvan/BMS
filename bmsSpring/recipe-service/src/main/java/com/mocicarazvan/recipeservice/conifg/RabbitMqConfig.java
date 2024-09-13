package com.mocicarazvan.recipeservice.conifg;


import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSenderWrapper;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqApprovedSenderWrapperImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;


@Configuration
public class RabbitMqConfig {
    @Value("${recipe.queue.name}")
    private String recipeQueueName;

    @Value("${recipe.exchange.name}")
    private String recipeExchangeName;

    @Value("${recipe.routing.key}")
    private String recipeRoutingKey;

    @Value("${recipe.extraLink}")
    private String extraLink;

    @Bean
    public Queue recipeQueue() {
        return new Queue(recipeQueueName, true);
    }

    @Bean
    public DirectExchange recipeExchange() {
        return new DirectExchange(recipeExchangeName);
    }

    @Bean
    public Binding recipeBinding(Queue recipeQueue, DirectExchange recipeExchange) {
        return BindingBuilder.bind(recipeQueue).to(recipeExchange).with(recipeRoutingKey);
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
        return new RabbitMqSenderImpl(recipeExchangeName, recipeRoutingKey, rabbitTemplate);
    }

    @Bean
    public RabbitMqApprovedSenderWrapper<RecipeResponse> recipeResponseRabbitMqApprovedSenderWrapper(RabbitMqSender rabbitMqSender) {
        return new RabbitMqApprovedSenderWrapperImpl<>(extraLink, rabbitMqSender);
    }
}

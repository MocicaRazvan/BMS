package com.mocicarazvan.ingredientservice.config;

import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteServiceImpl;
import com.mocicarazvan.templatemodule.utils.SimpleAsyncTaskExecutorInstance;
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
    @Value("${ingredient.delete.queue.name}")
    private String ingredientDeleteQueueName;

    @Value("${ingredient.update.queue.name}")
    private String ingredientUpdateQueueName;

    @Value("${ingredient.exchange.name}")
    private String ingredientExchangeName;

    @Value("${ingredient.delete.routing.key}")
    private String ingredientDeleteRoutingKey;

    @Value("${ingredient.update.routing.key}")
    private String ingredientUpdateRoutingKey;

    @Value("${nutritionalFact.delete.queue.name}")
    private String nutritionalFactDeleteQueueName;

    @Value("${nutritionalFact.update.queue.name}")
    private String nutritionalFactUpdateQueueName;

    @Value("${nutritionalFact.exchange.name}")
    private String nutritionalFactExchangeName;

    @Value("${nutritionalFact.delete.routing.key}")
    private String nutritionalFactDeleteRoutingKey;

    @Value("${nutritionalFact.update.routing.key}")
    private String nutritionalFactUpdateRoutingKey;


    @Bean
    public Queue ingredientDeleteQueue() {
        return new Queue(ingredientDeleteQueueName, true);
    }

    @Bean
    public Queue ingredientUpdateQueue() {
        return new Queue(ingredientUpdateQueueName, true);
    }

    @Bean
    public DirectExchange ingredientExchange() {
        return new DirectExchange(ingredientExchangeName);
    }

    @Bean
    public Binding ingredientDeleteBinding(DirectExchange ingredientExchange) {
        return BindingBuilder.bind(ingredientDeleteQueue())
                .to(ingredientExchange).with(ingredientDeleteRoutingKey);
    }

    @Bean
    public Binding ingredientUpdateBinding(DirectExchange ingredientExchange) {
        return BindingBuilder.bind(ingredientUpdateQueue())
                .to(ingredientExchange).with(ingredientUpdateRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setTaskExecutor(new SimpleAsyncTaskExecutorInstance().initialize());
        return rabbitTemplate;
    }

    @Bean
    public RabbitMqUpdateDeleteService<Ingredient> ingredientRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<Ingredient>builder()
                .deleteSender(new RabbitMqSenderImpl(ingredientExchangeName, ingredientDeleteRoutingKey, rabbitTemplate))
                .updateSender(new RabbitMqSenderImpl(ingredientExchangeName, ingredientUpdateRoutingKey, rabbitTemplate))
                .build();
    }

    @Bean
    public Queue nutritionalFactDeleteQueue() {
        return new Queue(nutritionalFactDeleteQueueName, true);
    }

    @Bean
    public Queue nutritionalFactUpdateQueue() {
        return new Queue(nutritionalFactUpdateQueueName, true);
    }

    @Bean
    public DirectExchange nutritionalFactExchange() {
        return new DirectExchange(nutritionalFactExchangeName);
    }

    @Bean
    public Binding nutritionalFactDeleteBinding(DirectExchange nutritionalFactExchange) {
        return BindingBuilder.bind(nutritionalFactDeleteQueue())
                .to(nutritionalFactExchange).with(nutritionalFactDeleteRoutingKey);
    }

    @Bean
    public Binding nutritionalFactUpdateBinding(DirectExchange nutritionalFactExchange) {
        return BindingBuilder.bind(nutritionalFactUpdateQueue())
                .to(nutritionalFactExchange).with(nutritionalFactUpdateRoutingKey);
    }


    @Bean
    public RabbitMqUpdateDeleteService<NutritionalFact> nutritionalFactRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<NutritionalFact>builder()
                .deleteSender(new RabbitMqSenderImpl(nutritionalFactExchangeName, nutritionalFactDeleteRoutingKey, rabbitTemplate))
                .updateSender(new RabbitMqSenderImpl(nutritionalFactExchangeName, nutritionalFactUpdateRoutingKey, rabbitTemplate))
                .build();
    }
}

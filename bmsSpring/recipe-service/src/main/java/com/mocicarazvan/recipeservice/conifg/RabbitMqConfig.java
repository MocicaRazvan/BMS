package com.mocicarazvan.recipeservice.conifg;


import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSender;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqApprovedSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteServiceImpl;
import com.mocicarazvan.templatemodule.utils.SimpleTaskExecutorsInstance;
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
    @Value("${recipe.queue.name}")
    private String recipeQueueName;

    @Value("${recipe.exchange.name}")
    private String recipeExchangeName;

    @Value("${recipe.routing.key}")
    private String recipeRoutingKey;

    @Value("${recipe.extraLink}")
    private String extraLink;

    @Value("${recipe.delete.queue.name}")
    private String recipeDeleteQueueName;

    @Value("${recipe.update.queue.name}")
    private String recipeUpdateQueueName;

    @Value("${recipe.delete.routing.key}")
    private String recipeDeleteRoutingKey;

    @Value("${recipe.update.routing.key}")
    private String recipeUpdateRoutingKey;

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:128}")
    private int executorAsyncConcurrencyLimit;

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
        rabbitTemplate.setTaskExecutor(new SimpleTaskExecutorsInstance().initializeVirtual(executorAsyncConcurrencyLimit));
        return rabbitTemplate;
    }

    @Bean
    public RabbitMqSender rabbitMqSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(recipeExchangeName, recipeRoutingKey, rabbitTemplate);
    }

    @Bean
    public RabbitMqApprovedSender<RecipeResponse> recipeResponseRabbitMqApprovedSenderWrapper(RabbitMqSender rabbitMqSender) {
        return new RabbitMqApprovedSenderImpl<>(extraLink, rabbitMqSender);
    }

    @Bean
    public Queue recipeDeleteQueue() {
        return new Queue(recipeDeleteQueueName, true);
    }

    @Bean
    public Queue recipeUpdateQueue() {
        return new Queue(recipeUpdateQueueName, true);
    }

    @Bean
    public Binding recipeDeleteBinding(DirectExchange recipeExchange) {
        return BindingBuilder.bind(recipeDeleteQueue())
                .to(recipeExchange).with(recipeDeleteRoutingKey);
    }

    @Bean
    public Binding recipeUpdateBinding(DirectExchange recipeExchange) {
        return BindingBuilder.bind(recipeUpdateQueue())
                .to(recipeExchange).with(recipeUpdateRoutingKey);
    }

    @Bean
    public RabbitMqUpdateDeleteService<Recipe> recipeRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<Recipe>builder()
                .deleteSender(new RabbitMqSenderImpl(recipeExchangeName, recipeDeleteRoutingKey, rabbitTemplate))
                .updateSender(new RabbitMqSenderImpl(recipeExchangeName, recipeUpdateRoutingKey, rabbitTemplate))
                .build();
    }
}

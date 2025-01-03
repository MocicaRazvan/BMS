package com.mocicarazvan.userservice.config;

import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteServiceImpl;
import com.mocicarazvan.templatemodule.utils.SimpleTaskExecutorsInstance;
import com.mocicarazvan.userservice.models.UserCustom;
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
    @Value("${user.delete.queue.name}")
    private String userDeleteQueueName;

    @Value("${user.update.queue.name}")
    private String userUpdateQueueName;

    @Value("${user.exchange.name}")
    private String userExchangeName;

    @Value("${user.delete.routing.key}")
    private String userDeleteRoutingKey;

    @Value("${user.update.routing.key}")
    private String userUpdateRoutingKey;

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:128}")
    private int executorAsyncConcurrencyLimit;

    @Bean
    public Queue userDeleteQueue() {
        return new Queue(userDeleteQueueName, true);
    }

    @Bean
    public Queue userUpdateQueue() {
        return new Queue(userUpdateQueueName, true);
    }

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(userExchangeName);
    }

    @Bean
    public Binding userDeleteBinding(DirectExchange userExchange) {
        return BindingBuilder.bind(userDeleteQueue())
                .to(userExchange).with(userDeleteRoutingKey);
    }

    @Bean
    public Binding userUpdateBinding(DirectExchange userExchange) {
        return BindingBuilder.bind(userUpdateQueue())
                .to(userExchange).with(userUpdateRoutingKey);
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
    public RabbitMqUpdateDeleteService<UserCustom> userRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<UserCustom>builder()
                .deleteSender(new RabbitMqSenderImpl(userExchangeName, userDeleteRoutingKey, rabbitTemplate))
                .updateSender(new RabbitMqSenderImpl(userExchangeName, userUpdateRoutingKey, rabbitTemplate))
                .build();
    }
}

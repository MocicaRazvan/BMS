package com.mocicarazvan.userservice.config;

import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteServiceImpl;
import com.mocicarazvan.templatemodule.utils.SimpleTaskExecutorsInstance;
import com.mocicarazvan.userservice.models.UserCustom;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.retry.support.RetryTemplate;

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

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:64}")
    private int executorAsyncConcurrencyLimit;

    @Value("${spring.custom.rabbitmq.concurrency:8}")
    private int concurrency;

    @Value("${user.fanout.exchange.name}")
    private String userFanoutExchangeName;

    @Bean
    public Queue userDeleteQueue() {
        return new Queue(userDeleteQueueName, true);
    }

    @Bean
    public Queue userUpdateQueue() {
        return new Queue(userUpdateQueueName, true);
    }

    @Bean
    public Queue userCacheInvalidateQueue() {
        return new AnonymousQueue();
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
    public FanoutExchange userFanoutExchange() {
        return new FanoutExchange(userFanoutExchangeName);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleAsyncTaskExecutor rabbitMqAsyncTaskExecutor() {
        return new SimpleTaskExecutorsInstance().initializeVirtual(executorAsyncConcurrencyLimit);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         @Qualifier("rabbitMqAsyncTaskExecutor") SimpleAsyncTaskExecutor rabbitMqAsyncTaskExecutor
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setTaskExecutor(rabbitMqAsyncTaskExecutor);
        rabbitTemplate.setRetryTemplate(RetryTemplate.defaultInstance());
        return rabbitTemplate;
    }

    @Bean
    public RabbitMqUpdateDeleteService<UserCustom> userRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<UserCustom>builder()
                .deleteSender(new RabbitMqSenderImpl(userExchangeName, userDeleteRoutingKey, rabbitTemplate, concurrency))
                .updateSender(new RabbitMqSenderImpl(userExchangeName, userUpdateRoutingKey, rabbitTemplate, concurrency))
                .build();
    }

    @Bean
    public RabbitMqSender userCacheInvalidateSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(userFanoutExchangeName, "", rabbitTemplate, concurrency);
    }

    @Bean
    public Queue usersEmailsRpcQueue() {
        return new Queue("users.rpc.queue", true, false, true);
    }

    @Bean
    public Queue usersEmailExistsQueue() {
        return new Queue("users.email.exists", true, false, true);
    }

    @Bean
    public DirectExchange usersRpcExchange() {
        return new DirectExchange("users.rpc.exchange", true, false);
    }

    @Bean
    public Binding usersEmailsRpcBinding(Queue usersEmailsRpcQueue, DirectExchange usersRpcExchange) {
        return BindingBuilder
                .bind(usersEmailsRpcQueue)
                .to(usersRpcExchange)
                .with("users.rpc.getUsersByEmails");
    }

    @Bean
    public Binding userEmailExistsBinding(Queue usersEmailExistsQueue, DirectExchange usersRpcExchange) {
        return BindingBuilder
                .bind(usersEmailExistsQueue)
                .to(usersRpcExchange)
                .with("users.rpc.existsUserByEmail");
    }

}

package com.mocicarazvan.commentservice.config;

import com.mocicarazvan.commentservice.models.Comment;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteServiceImpl;
import com.mocicarazvan.templatemodule.utils.SimpleTaskExecutorsInstance;
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
    @Value("${comment.delete.queue.name}")
    private String commentDeleteQueueName;

    @Value("${comment.update.queue.name}")
    private String commentUpdateQueueName;

    @Value("${comment.exchange.name}")
    private String commentExchangeName;

    @Value("${comment.delete.routing.key}")
    private String commentDeleteRoutingKey;

    @Value("${comment.update.routing.key}")
    private String commentUpdateRoutingKey;

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:64}")
    private int executorAsyncConcurrencyLimit;

    @Value("${spring.custom.rabbitmq.concurrency:8}")
    private int concurrency;

    @Value("${comment.fanout.exchange.name}")
    private String commentFanoutExchangeName;

    @Bean
    public Queue commentDeleteQueue() {
        return new Queue(commentDeleteQueueName, true);
    }

    @Bean
    public Queue commentUpdateQueue() {
        return new Queue(commentUpdateQueueName, true);
    }

    @Bean
    public DirectExchange commentExchange() {
        return new DirectExchange(commentExchangeName);
    }

    @Bean
    public Queue commentCacheInvalidateQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding commentDeleteBinding(DirectExchange commentExchange) {
        return BindingBuilder.bind(commentDeleteQueue())
                .to(commentExchange).with(commentDeleteRoutingKey);
    }

    @Bean
    public Binding commentUpdateBinding(DirectExchange commentExchange) {
        return BindingBuilder.bind(commentUpdateQueue())
                .to(commentExchange).with(commentUpdateRoutingKey);
    }

    @Bean
    public FanoutExchange commentFanoutExchange() {
        return new FanoutExchange(commentFanoutExchangeName);
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
    public RabbitMqSender commentCacheInvalidateSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(commentFanoutExchangeName, "", rabbitTemplate, concurrency);
    }

    @Bean
    public RabbitMqUpdateDeleteService<Comment> commentRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<Comment>builder()
                .deleteSender(new RabbitMqSenderImpl(commentExchangeName, commentDeleteRoutingKey, rabbitTemplate, concurrency))
                .updateSender(new RabbitMqSenderImpl(commentExchangeName, commentUpdateRoutingKey, rabbitTemplate, concurrency))
                .build();
    }
}

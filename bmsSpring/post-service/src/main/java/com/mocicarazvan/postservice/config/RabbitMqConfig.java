package com.mocicarazvan.postservice.config;

import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.postservice.models.Post;
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
    @Value("${post.queue.name}")
    private String postQueueName;

    @Value("${post.exchange.name}")
    private String postExchangeName;

    @Value("${post.routing.key}")
    private String postRoutingKey;

    @Value("${post.extraLink}")
    private String extraLink;


    @Value("${post.delete.queue.name}")
    private String postDeleteQueueName;

    @Value("${post.update.queue.name}")
    private String postUpdateQueueName;

    @Value("${post.delete.routing.key}")
    private String postDeleteRoutingKey;

    @Value("${post.update.routing.key}")
    private String postUpdateRoutingKey;

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:128}")
    private int executorAsyncConcurrencyLimit;

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
        rabbitTemplate.setTaskExecutor(new SimpleTaskExecutorsInstance().initializeVirtual(executorAsyncConcurrencyLimit));
        return rabbitTemplate;
    }

    @Bean
    public RabbitMqSender rabbitMqSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(postExchangeName, postRoutingKey, rabbitTemplate);
    }

    @Bean
    public RabbitMqApprovedSender<PostResponse> postResponseRabbitMqApprovedSenderWrapper(RabbitMqSender rabbitMqSender) {
        return new RabbitMqApprovedSenderImpl<>(extraLink, rabbitMqSender);
    }

    @Bean
    public Queue postDeleteQueue() {
        return new Queue(postDeleteQueueName, true);
    }

    @Bean
    public Queue postUpdateQueue() {
        return new Queue(postUpdateQueueName, true);
    }

    @Bean
    public Binding postDeleteBinding(DirectExchange postExchange) {
        return BindingBuilder.bind(postDeleteQueue())
                .to(postExchange).with(postDeleteRoutingKey);
    }

    @Bean
    public Binding postUpdateBinding(DirectExchange postExchange) {
        return BindingBuilder.bind(postUpdateQueue())
                .to(postExchange).with(postUpdateRoutingKey);
    }

    @Bean
    public RabbitMqUpdateDeleteService<Post> postRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<Post>builder()
                .deleteSender(new RabbitMqSenderImpl(postExchangeName, postDeleteRoutingKey, rabbitTemplate))
                .updateSender(new RabbitMqSenderImpl(postExchangeName, postUpdateRoutingKey, rabbitTemplate))
                .build();
    }
}

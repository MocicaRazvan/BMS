package com.mocicarazvan.postservice.config;

import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSender;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqApprovedSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteServiceImpl;
import com.mocicarazvan.templatemodule.utils.RabbitMqQueueUtils;
import com.mocicarazvan.templatemodule.utils.SimpleTaskExecutorsInstance;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class RabbitMqConfig implements RabbitListenerConfigurer {
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

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:64}")
    private int executorAsyncConcurrencyLimit;

    @Value("${spring.custom.rabbitmq.concurrency:8}")
    private int concurrency;

    @Value("${post.fanout.exchange.name}")
    private String postFanoutExchangeName;

    private final LocalValidatorFactoryBean validator;

    public RabbitMqConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

    //    @Bean
//    public Queue postQueue() {
//        return new Queue(postQueueName, true);
//    }
    @Bean
    public Queue postQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(postQueueName);
    }

    @Bean
    public Queue postDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(postQueueName);
    }

    @Bean
    public Queue postCacheInvalidateQueue() {
        return new AnonymousQueue();
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
    public FanoutExchange postFanoutExchange() {
        return new FanoutExchange(postFanoutExchangeName);
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
                                         @Qualifier("rabbitMqAsyncTaskExecutor") SimpleAsyncTaskExecutor rabbitMqAsyncTaskExecutor) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setTaskExecutor(rabbitMqAsyncTaskExecutor);
        rabbitTemplate.setRetryTemplate(RetryTemplate.defaultInstance());
        return rabbitTemplate;
    }

//    @Bean
//    public RabbitMqSender rabbitMqSender(RabbitTemplate rabbitTemplate) {
//        return new RabbitMqSenderImpl(postExchangeName, postRoutingKey, rabbitTemplate, concurrency);
//    }

    @Bean
    public RabbitMqApprovedSender<PostResponse> postResponseRabbitMqApprovedSenderWrapper(RabbitTemplate rabbitTemplate) {
        return new RabbitMqApprovedSenderImpl<>(extraLink, new RabbitMqSenderImpl(postExchangeName, postRoutingKey, rabbitTemplate, concurrency));
    }

    @Bean
    public RabbitMqSender postCacheInvalidateSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(postFanoutExchangeName, "", rabbitTemplate, concurrency);
    }

//    @Bean
//    public Queue postDeleteQueue() {
//        return new Queue(postDeleteQueueName, true);
//    }

    @Bean
    public Queue postDeleteQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(postDeleteQueueName);
    }

    @Bean
    public Queue postDeleteDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(postDeleteQueueName);
    }

//    @Bean
//    public Queue postUpdateQueue() {
//        return new Queue(postUpdateQueueName, true);
//    }

    @Bean
    public Queue postUpdateQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(postUpdateQueueName);
    }

    @Bean
    public Queue postUpdateDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(postUpdateQueueName);
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
    public Binding postCacheInvalidateBinding(@Qualifier("postCacheInvalidateQueue") Queue postCacheInvalidateQueue,
                                              @Qualifier("postFanoutExchange") FanoutExchange postFanoutExchange) {
        return BindingBuilder.bind(postCacheInvalidateQueue).to(postFanoutExchange);
    }

    @Bean
    public RabbitMqUpdateDeleteService<Post> postRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<Post>builder()
                .deleteSender(new RabbitMqSenderImpl(postExchangeName, postDeleteRoutingKey, rabbitTemplate, concurrency))
                .updateSender(new RabbitMqSenderImpl(postExchangeName, postUpdateRoutingKey, rabbitTemplate, concurrency))
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            @Qualifier("rabbitMqAsyncTaskExecutor") SimpleAsyncTaskExecutor taskExecutor
    ) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setTaskExecutor(taskExecutor);
        return factory;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setValidator(validator);
    }
}

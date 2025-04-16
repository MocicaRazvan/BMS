package com.mocicarazvan.ingredientservice.config;

import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
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

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:64}")
    private int executorAsyncConcurrencyLimit;

    @Value("${spring.custom.rabbitmq.concurrency:8}")
    private int concurrency;

    @Value("${ingredient.fanout.exchange.name}")
    private String ingredientFanoutExchangeName;

    private final LocalValidatorFactoryBean validator;

    public RabbitMqConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

//    @Bean
//    public Queue ingredientDeleteQueue() {
//        return new Queue(ingredientDeleteQueueName, true);
//    }

    @Bean
    public Queue ingredientDeleteQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(ingredientDeleteQueueName);
    }

    @Bean
    public Queue ingredientDeleteDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(ingredientDeleteQueueName);
    }

//    @Bean
//    public Queue ingredientUpdateQueue() {
//        return new Queue(ingredientUpdateQueueName, true);
//    }

    @Bean
    public Queue ingredientUpdateQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(ingredientUpdateQueueName);
    }

    @Bean
    public Queue ingredientUpdateDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(ingredientUpdateQueueName);
    }

    @Bean
    public Queue ingredientCacheInvalidateQueue() {
        return new AnonymousQueue();
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
    public FanoutExchange ingredientFanoutExchange() {
        return new FanoutExchange(ingredientFanoutExchangeName);
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

    @Bean
    public RabbitMqUpdateDeleteService<Ingredient> ingredientRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<Ingredient>builder()
                .deleteSender(new RabbitMqSenderImpl(ingredientExchangeName, ingredientDeleteRoutingKey, rabbitTemplate, concurrency))
                .updateSender(new RabbitMqSenderImpl(ingredientExchangeName, ingredientUpdateRoutingKey, rabbitTemplate, concurrency))
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
    public Binding ingredientCacheInvalidateBinding(@Qualifier("ingredientCacheInvalidateQueue") Queue ingredientCacheInvalidateQueue,
                                                    @Qualifier("ingredientFanoutExchange") FanoutExchange ingredientFanoutExchange) {
        return BindingBuilder.bind(ingredientCacheInvalidateQueue).to(ingredientFanoutExchange);
    }

    @Bean
    public RabbitMqUpdateDeleteService<NutritionalFact> nutritionalFactRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<NutritionalFact>builder()
                .deleteSender(new RabbitMqSenderImpl(nutritionalFactExchangeName, nutritionalFactDeleteRoutingKey, rabbitTemplate, concurrency))
                .updateSender(new RabbitMqSenderImpl(nutritionalFactExchangeName, nutritionalFactUpdateRoutingKey, rabbitTemplate, concurrency))
                .build();
    }

    @Bean
    public RabbitMqSender ingredientCacheInvalidateSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(ingredientFanoutExchangeName, "", rabbitTemplate, concurrency);
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

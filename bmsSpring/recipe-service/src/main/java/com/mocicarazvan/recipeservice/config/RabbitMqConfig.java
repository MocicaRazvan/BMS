package com.mocicarazvan.recipeservice.config;


import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.recipeservice.models.Recipe;
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

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:64}")
    private int executorAsyncConcurrencyLimit;

    @Value("${spring.custom.rabbitmq.concurrency:8}")
    private int concurrency;

    @Value("${recipe.fanout.exchange.name}")
    private String recipeFanoutExchangeName;

    private final LocalValidatorFactoryBean validator;

    public RabbitMqConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

//    @Bean
//    public Queue recipeQueue() {
//        return new Queue(recipeQueueName, true);
//    }

    @Bean
    public Queue recipeQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(recipeQueueName);
    }

    @Bean
    public Queue recipeDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(recipeDeleteQueueName);
    }

    @Bean
    public Queue recipeCacheInvalidateQueue() {
        return new AnonymousQueue();
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
    public FanoutExchange recipeFanoutExchange() {
        return new FanoutExchange(recipeFanoutExchangeName);
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
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory
            , @Qualifier("rabbitMqAsyncTaskExecutor") SimpleAsyncTaskExecutor rabbitMqAsyncTaskExecutor

    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setTaskExecutor(rabbitMqAsyncTaskExecutor);
        rabbitTemplate.setRetryTemplate(RetryTemplate.defaultInstance());
        return rabbitTemplate;
    }


    @Bean
    public RabbitMqApprovedSender<RecipeResponse> recipeResponseRabbitMqApprovedSenderWrapper(RabbitTemplate rabbitTemplate) {
        return new RabbitMqApprovedSenderImpl<>(extraLink, new RabbitMqSenderImpl(recipeExchangeName, recipeRoutingKey, rabbitTemplate, concurrency));
    }

    @Bean
    public RabbitMqSender recipeCacheInvalidateSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(recipeFanoutExchangeName, "", rabbitTemplate, concurrency);
    }

//    @Bean
//    public Queue recipeDeleteQueue() {
//        return new Queue(recipeDeleteQueueName, true);
//    }

    @Bean
    public Queue recipeDeleteQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(recipeDeleteQueueName);
    }

    @Bean
    public Queue recipeDeleteDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(recipeDeleteQueueName);
    }

//    @Bean
//    public Queue recipeUpdateQueue() {
//        return new Queue(recipeUpdateQueueName, true);
//    }

    @Bean
    public Queue recipeUpdateQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(recipeUpdateQueueName);
    }

    @Bean
    public Queue recipeUpdateDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(recipeUpdateQueueName);
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
    public Binding recipeCacheInvalidateBinding(@Qualifier("recipeCacheInvalidateQueue") Queue recipeCacheInvalidateQueue,
                                                @Qualifier("recipeFanoutExchange") FanoutExchange recipeFanoutExchange) {
        return BindingBuilder.bind(recipeCacheInvalidateQueue).to(recipeFanoutExchange);
    }

    @Bean
    public RabbitMqUpdateDeleteService<Recipe> recipeRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<Recipe>builder()
                .deleteSender(new RabbitMqSenderImpl(recipeExchangeName, recipeDeleteRoutingKey, rabbitTemplate, concurrency))
                .updateSender(new RabbitMqSenderImpl(recipeExchangeName, recipeUpdateRoutingKey, rabbitTemplate, concurrency))
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

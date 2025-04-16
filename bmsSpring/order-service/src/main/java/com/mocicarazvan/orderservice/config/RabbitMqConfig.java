package com.mocicarazvan.orderservice.config;

import com.mocicarazvan.orderservice.models.Order;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteNoOpServiceImpl;
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
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class RabbitMqConfig implements RabbitListenerConfigurer {
    @Value("${plan.bought.queue.name}")
    private String planBoughtQueueName;

    @Value("${plan.bought.exchange.name}")
    private String planBoughtExchangeName;

    @Value("${plan.bought.routing.key}")
    private String planBoughtRoutingKey;

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:64}")
    private int executorAsyncConcurrencyLimit;

    @Value("${spring.custom.rabbitmq.concurrency:8}")
    private int concurrency;

    @Value("${order.fanout.exchange.name}")
    private String orderFanoutExchangeName;

    private final LocalValidatorFactoryBean validator;

    public RabbitMqConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }


//    @Bean
//    public Queue planBoughtQueue() {
//        return new Queue(planBoughtQueueName, true);
//    }

    @Bean
    public Queue planBoughtQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(planBoughtQueueName);
    }

    @Bean
    public Queue planBoughtDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(planBoughtQueueName);
    }

    @Bean
    public Queue orderCacheInvalidateQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public DirectExchange planBoughtExchange() {
        return new DirectExchange(planBoughtExchangeName);
    }

    @Bean
    public Binding planBoughtBinding(Queue planBoughtQueue, DirectExchange planBoughtExchange) {
        return BindingBuilder.bind(planBoughtQueue).to(planBoughtExchange).with(planBoughtRoutingKey);
    }

    @Bean
    public FanoutExchange orderFanoutExchange() {
        return new FanoutExchange(orderFanoutExchangeName);
    }

    @Bean
    public Binding orderCacheInvalidateBinding(@Qualifier("orderCacheInvalidateQueue") Queue orderCacheInvalidateQueue,
                                               @Qualifier("orderFanoutExchange") FanoutExchange orderFanoutExchange) {
        return BindingBuilder.bind(orderCacheInvalidateQueue).to(orderFanoutExchange);
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
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, @Qualifier("rabbitMqAsyncTaskExecutor") SimpleAsyncTaskExecutor rabbitMqAsyncTaskExecutor) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setTaskExecutor(rabbitMqAsyncTaskExecutor);
        rabbitTemplate.setRetryTemplate(RetryTemplate.defaultInstance());
        return rabbitTemplate;
    }

    @Bean
    public RabbitMqSender orderCacheInvalidateSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(orderFanoutExchangeName, "", rabbitTemplate, concurrency);
    }

    @Bean
    @Primary
    public RabbitMqSender rabbitMqSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(planBoughtExchangeName, planBoughtRoutingKey, rabbitTemplate, concurrency);
    }


    @Bean
    public RabbitMqUpdateDeleteService<Order> kanbanColumnRabbitMqUpdateDeleteService() {
        return new RabbitMqUpdateDeleteNoOpServiceImpl<>();
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

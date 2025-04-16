package com.mocicarazvan.kanbanservice.config;

import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteNoOpServiceImpl;
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

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:64}")
    private int executorAsyncConcurrencyLimit;

    @Value("${spring.custom.rabbitmq.concurrency:8}")
    private int concurrency;

    @Value("${kanban.fanout.exchange.name}")
    private String kanbanFanoutExchangeName;

    private final LocalValidatorFactoryBean validator;

    public RabbitMqConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }


    @Bean
    public Queue kanbanCacheInvalidateQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public FanoutExchange kanbanFanoutExchange() {
        return new FanoutExchange(kanbanFanoutExchangeName);
    }

    @Bean
    public Binding kanbanCacheInvalidateBinding(@Qualifier("kanbanCacheInvalidateQueue") Queue kanbanCacheInvalidateQueue,
                                                @Qualifier("kanbanFanoutExchange") FanoutExchange kanbanFanoutExchange) {
        return BindingBuilder.bind(kanbanCacheInvalidateQueue).to(kanbanFanoutExchange);
    }

    @Bean
    public SimpleAsyncTaskExecutor rabbitMqAsyncTaskExecutor() {
        return new SimpleTaskExecutorsInstance().initializeVirtual(executorAsyncConcurrencyLimit);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
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
    public RabbitMqSender kanbanCacheInvalidateSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(kanbanFanoutExchangeName, "", rabbitTemplate, concurrency);
    }

    @Bean
    public RabbitMqUpdateDeleteService<KanbanColumn> kanbanColumnRabbitMqUpdateDeleteService() {
        return new RabbitMqUpdateDeleteNoOpServiceImpl<>();
    }

    @Bean
    public RabbitMqUpdateDeleteService<KanbanTask> kanbanTaskRabbitMqUpdateDeleteService() {
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

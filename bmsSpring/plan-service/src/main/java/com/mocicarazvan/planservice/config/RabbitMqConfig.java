package com.mocicarazvan.planservice.config;

import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.planservice.models.Plan;
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
    @Value("${plan.queue.name}")
    private String planQueueName;

    @Value("${plan.exchange.name}")
    private String planExchangeName;

    @Value("${plan.routing.key}")
    private String planRoutingKey;

    @Value("${plan.extraLink}")
    private String extraLink;

    @Value("${plan.delete.queue.name}")
    private String planDeleteQueueName;

    @Value("${plan.update.queue.name}")
    private String planUpdateQueueName;

    @Value("${plan.delete.routing.key}")
    private String planDeleteRoutingKey;

    @Value("${plan.update.routing.key}")
    private String planUpdateRoutingKey;

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:64}")
    private int executorAsyncConcurrencyLimit;

    @Value("${spring.custom.rabbitmq.concurrency:8}")
    private int concurrency;

    @Value("${plan.fanout.exchange.name}")
    private String planFanoutExchangeName;

    private final LocalValidatorFactoryBean validator;

    public RabbitMqConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

//    @Bean
//    public Queue planQueue() {
//        return new Queue(planQueueName, true);
//    }

    @Bean
    public Queue planQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(planQueueName);
    }

    @Bean
    public Queue planDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(planQueueName);
    }

    @Bean
    public Queue planCacheInvalidateQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public DirectExchange planExchange() {
        return new DirectExchange(planExchangeName);
    }

    @Bean
    public Binding planBinding(Queue planQueue, DirectExchange planExchange) {
        return BindingBuilder.bind(planQueue).to(planExchange).with(planRoutingKey);
    }

    @Bean
    public FanoutExchange planFanoutExchange() {
        return new FanoutExchange(planFanoutExchangeName);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Binding planCacheInvalidateBinding(@Qualifier("planCacheInvalidateQueue") Queue planCacheInvalidateQueue,
                                              @Qualifier("planFanoutExchange") FanoutExchange planFanoutExchange) {
        return BindingBuilder.bind(planCacheInvalidateQueue).to(planFanoutExchange);
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
//        return new RabbitMqSenderImpl(planExchangeName, planRoutingKey, rabbitTemplate, concurrency);
//    }

    @Bean
    public RabbitMqApprovedSender<PlanResponse> planResponseRabbitMqApprovedSenderWrapper(RabbitTemplate rabbitTemplate) {
        return new RabbitMqApprovedSenderImpl<>(extraLink, new RabbitMqSenderImpl(planExchangeName, planRoutingKey, rabbitTemplate, concurrency));
    }

    @Bean
    public RabbitMqSender planCacheInvalidateSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(planFanoutExchangeName, "", rabbitTemplate, concurrency);
    }

//    @Bean
//    public Queue planDeleteQueue() {
//        return new Queue(planDeleteQueueName, true);
//    }

    @Bean
    public Queue planDeleteQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(planDeleteQueueName);
    }

    @Bean
    public Queue planDeleteDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(planDeleteQueueName);
    }

//    @Bean
//    public Queue planUpdateQueue() {
//        return new Queue(planUpdateQueueName, true);
//    }

    @Bean
    public Queue planUpdateQueue() {
        return RabbitMqQueueUtils.durableQueueWithDlq(planUpdateQueueName);
    }

    @Bean
    public Queue planUpdateDlqQueue() {
        return RabbitMqQueueUtils.deadLetterQueue(planUpdateQueueName);
    }

    @Bean
    public Binding planDeleteBinding(DirectExchange planExchange) {
        return BindingBuilder.bind(planDeleteQueue())
                .to(planExchange).with(planDeleteRoutingKey);
    }

    @Bean
    public Binding planUpdateBinding(DirectExchange planExchange) {
        return BindingBuilder.bind(planUpdateQueue())
                .to(planExchange).with(planUpdateRoutingKey);
    }

    @Bean
    public RabbitMqUpdateDeleteService<Plan> planRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<Plan>builder()
                .deleteSender(new RabbitMqSenderImpl(planExchangeName, planDeleteRoutingKey, rabbitTemplate, concurrency))
                .updateSender(new RabbitMqSenderImpl(planExchangeName, planUpdateRoutingKey, rabbitTemplate, concurrency))
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

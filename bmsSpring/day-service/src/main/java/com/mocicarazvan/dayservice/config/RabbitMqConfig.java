package com.mocicarazvan.dayservice.config;

import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.dayservice.models.Meal;
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

@Configuration
public class RabbitMqConfig {
    @Value("${day.delete.queue.name}")
    private String dayDeleteQueueName;

    @Value("${day.update.queue.name}")
    private String dayUpdateQueueName;

    @Value("${day.exchange.name}")
    private String dayExchangeName;

    @Value("${day.delete.routing.key}")
    private String dayDeleteRoutingKey;

    @Value("${day.update.routing.key}")
    private String dayUpdateRoutingKey;

    @Value("${meal.delete.queue.name}")
    private String mealDeleteQueueName;

    @Value("${meal.update.queue.name}")
    private String mealUpdateQueueName;

    @Value("${meal.exchange.name}")
    private String mealExchangeName;

    @Value("${meal.delete.routing.key}")
    private String mealDeleteRoutingKey;

    @Value("${meal.update.routing.key}")
    private String mealUpdateRoutingKey;

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:64}")
    private int executorAsyncConcurrencyLimit;

    @Value("${spring.custom.rabbitmq.concurrency:8}")
    private int concurrency;

    @Value("${day.fanout.exchange.name}")
    private String dayFanoutExchangeName;

    @Bean
    public Queue dayDeleteQueue() {
        return new Queue(dayDeleteQueueName, true);
    }

    @Bean
    public Queue dayUpdateQueue() {
        return new Queue(dayUpdateQueueName, true);
    }

    @Bean
    public DirectExchange dayExchange() {
        return new DirectExchange(dayExchangeName);
    }

    @Bean
    public Queue dayCacheInvalidateQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding dayDeleteBinding(DirectExchange dayExchange) {
        return BindingBuilder.bind(dayDeleteQueue())
                .to(dayExchange).with(dayDeleteRoutingKey);
    }

    @Bean
    public Binding dayUpdateBinding(DirectExchange dayExchange) {
        return BindingBuilder.bind(dayUpdateQueue())
                .to(dayExchange).with(dayUpdateRoutingKey);
    }

    @Bean
    public FanoutExchange dayFanoutExchange() {
        return new FanoutExchange(dayFanoutExchangeName);
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
        return rabbitTemplate;
    }

    @Bean
    public RabbitMqUpdateDeleteService<Day> dayRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<Day>builder()
                .deleteSender(new RabbitMqSenderImpl(dayExchangeName, dayDeleteRoutingKey, rabbitTemplate, concurrency))
                .updateSender(new RabbitMqSenderImpl(dayExchangeName, dayUpdateRoutingKey, rabbitTemplate, concurrency))
                .build();
    }

    @Bean
    public Queue mealDeleteQueue() {
        return new Queue(mealDeleteQueueName, true);
    }

    @Bean
    public Queue mealUpdateQueue() {
        return new Queue(mealUpdateQueueName, true);
    }

    @Bean
    public DirectExchange mealExchange() {
        return new DirectExchange(mealExchangeName);
    }

    @Bean
    public Binding mealDeleteBinding(DirectExchange mealExchange) {
        return BindingBuilder.bind(mealDeleteQueue())
                .to(mealExchange).with(mealDeleteRoutingKey);
    }

    @Bean
    public Binding mealUpdateBinding(DirectExchange mealExchange) {
        return BindingBuilder.bind(mealUpdateQueue())
                .to(mealExchange).with(mealUpdateRoutingKey);
    }


    @Bean
    public RabbitMqUpdateDeleteService<Meal> mealRabbitMqUpdateDeleteService(RabbitTemplate rabbitTemplate) {
        return RabbitMqUpdateDeleteServiceImpl.<Meal>builder()
                .deleteSender(new RabbitMqSenderImpl(mealExchangeName, mealDeleteRoutingKey, rabbitTemplate, concurrency))
                .updateSender(new RabbitMqSenderImpl(mealExchangeName, mealUpdateRoutingKey, rabbitTemplate, concurrency))
                .build();
    }

    @Bean
    public RabbitMqSender dayCacheInvalidateSender(RabbitTemplate rabbitTemplate) {
        return new RabbitMqSenderImpl(dayFanoutExchangeName, "", rabbitTemplate, concurrency);
    }
}

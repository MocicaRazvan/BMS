package com.mocicarazvan.kanbanservice.config;

import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteNoOpServiceImpl;
import com.mocicarazvan.templatemodule.utils.SimpleTaskExecutorsInstance;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
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

    @Value("${spring.custom.rabbit.thread.pool.executorAsyncConcurrencyLimit:64}")
    private int executorAsyncConcurrencyLimit;

    @Value("${spring.custom.rabbitmq.concurrency:8}")
    private int concurrency;

    @Value("${kanban.fanout.exchange.name}")
    private String kanbanFanoutExchangeName;

    @Bean
    public Queue kanbanCacheInvalidateQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public FanoutExchange kanbanFanoutExchange() {
        return new FanoutExchange(kanbanFanoutExchangeName);
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
}

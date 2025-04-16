package com.mocicarazvan.templatemodule.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.testUtils.RabbitTestUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@ImportAutoConfiguration({
        RabbitAutoConfiguration.class,
        PropertyPlaceholderAutoConfiguration.class
})
public class RabbitMqTestConfig {

    public static final String TEST_QUEUE = "testQueue";
    public static final String TEST_EXCHANGE = "testExchange";
    public static final String TEST_ROUTING_KEY = "testRoutingKey";


    @Bean
    public Queue testQueue() {
        return new Queue(TEST_QUEUE, true);
    }

    @Bean
    public DirectExchange testExchange() {
        return new DirectExchange(TEST_EXCHANGE, true, true);
    }

    @Bean
    public Binding testBinding(
            @Qualifier("testQueue") Queue testQueue,
            @Qualifier("testExchange") DirectExchange testExchange
    ) {
        return BindingBuilder.bind(testQueue).to(testExchange).with(TEST_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }


    @Bean
    public RabbitTestUtils rabbitTestUtils(RabbitTemplate rabbitTemplate) {
        return new RabbitTestUtils(new ObjectMapper().findAndRegisterModules(), rabbitTemplate);
    }

}

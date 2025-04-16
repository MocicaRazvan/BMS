package com.mocicarazvan.websocketservice.listeners;

import com.mocicarazvan.websocketservice.config.AsyncConfig;
import com.mocicarazvan.websocketservice.config.RabbitMqConfig;
import com.mocicarazvan.websocketservice.config.TestContainerImages;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@Execution(ExecutionMode.SAME_THREAD)
@Testcontainers
@ContextConfiguration(classes = {AsyncConfig.class, RabbitMqConfig.class})
@ImportAutoConfiguration({RabbitAutoConfiguration.class, ValidationAutoConfiguration.class})
@TestPropertySource(locations = "classpath:application.properties")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseListenerTestClass {
    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(TestContainerImages.RABBIT_MQ_IMAGE)
            .withExposedPorts(5672, 15672)
            .withEnv("RABBITMQ_DEFAULT_USER", "guest")
            .withEnv("RABBITMQ_DEFAULT_PASS", "guest");

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory;


    @Autowired
    RabbitProperties rabbitProperties;

    @Autowired
    @Qualifier("scheduledExecutorService")
    SimpleAsyncTaskExecutor scheduledExecutorService;


    @Test
    @Order(1)
    void loads_base() {
        assertNotNull(simpleRabbitListenerContainerFactory);
        Object executor = ReflectionTestUtils.getField(simpleRabbitListenerContainerFactory, "taskExecutor");
        assertNotNull(executor);
        assertSame(scheduledExecutorService, executor);
        assertTrue(rabbitProperties.getListener().getSimple().getRetry().getMaxAttempts() > 2);
    }

}

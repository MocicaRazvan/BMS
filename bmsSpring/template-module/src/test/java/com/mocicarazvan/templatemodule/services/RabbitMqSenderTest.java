package com.mocicarazvan.templatemodule.services;


import com.mocicarazvan.templatemodule.config.RabbitMqTestConfig;
import com.mocicarazvan.templatemodule.config.TestContainerImages;
import com.mocicarazvan.templatemodule.models.IdGenerated;
import com.mocicarazvan.templatemodule.models.IdGeneratedImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.testUtils.RabbitTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(RabbitMqTestConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RabbitMqSenderTest {
    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(TestContainerImages.RABBIT_MQ_IMAGE)
            .withExposedPorts(5672, 15672)
            .withEnv("RABBITMQ_DEFAULT_USER", "guest")
            .withEnv("RABBITMQ_DEFAULT_PASS", "guest");
    @SpyBean
    private RabbitTemplate template;

    @Autowired
    private RabbitTestUtils rabbitTestUtils;


    private RabbitMqSender rabbitMqSender;

    @BeforeEach
    void setUp() {
        rabbitMqSender = new RabbitMqSenderImpl(RabbitMqTestConfig.TEST_EXCHANGE,
                RabbitMqTestConfig.TEST_ROUTING_KEY, template, 10);
    }


    @ParameterizedTest
    @NullAndEmptySource
    void checkArgsEmptyMessages_shouldThrow(List<String> messages) {
        var ex = assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(rabbitMqSender, "checkArgs", messages);
        });
        assertEquals("Message cannot be null or empty", ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void checkArgsEmptyExchange_shouldThrow(String exchange) {
        ReflectionTestUtils.setField(rabbitMqSender, "exchangeName", exchange);
        var ex = assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(rabbitMqSender, "checkArgs", List.of(1, 2));
        });
        assertEquals("Exchange name cannot be null or empty", ex.getMessage());
    }

    @Test
    void checkArgsNullRouting_shouldThrow() {
        ReflectionTestUtils.setField(rabbitMqSender, "routingKey", null);
        var ex = assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(rabbitMqSender, "checkArgs", List.of(1, 2));
        });
        assertEquals("Routing key cannot be null", ex.getMessage());
    }

    @Test
    void checkArgs_success() {
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(rabbitMqSender, "checkArgs", List.of(1, 2));
        });

    }

    @Test
    void sendMessage_success() {
        IdGeneratedImpl message = IdGeneratedImpl.builder().id(1L).createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
        assertDoesNotThrow(() -> rabbitMqSender.sendMessage(message));

        var messages = rabbitTestUtils.drainTestQueue(RabbitMqTestConfig.TEST_QUEUE,
                new ParameterizedTypeReference<IdGeneratedImpl>() {
                }, 1000, 1);

        assertEquals(1, messages.size());
        assertEquals(message, messages.get(0));
    }

    @Test
    void sendMessage_error() {

        assertThrows(IllegalArgumentException.class, () -> {
            rabbitMqSender.sendMessage(null);
        });
        verify(template, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void sendBatchMessage_success() {
        List<IdGeneratedImpl> payload = IntStream.range(0, 10)
                .mapToObj(i -> IdGeneratedImpl.builder().id((long) i).createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now()).build())
                .collect(Collectors.toList());
        assertDoesNotThrow(() -> rabbitMqSender.sendBatchMessage(payload));

        var messages = rabbitTestUtils.drainTestQueue(RabbitMqTestConfig.TEST_QUEUE,
                new ParameterizedTypeReference<IdGeneratedImpl>() {
                }, 1000, payload.size());

        assertEquals(payload.size(), messages.size());
        assertEquals(payload.stream().sorted(
                Comparator.comparing(IdGenerated::getId)).collect(Collectors.toList()

        ), messages.stream().sorted(
                Comparator.comparing(IdGenerated::getId)).collect(Collectors.toList()));
    }

    @Test
    void sendBatchMessage_error() {
        assertThrows(IllegalArgumentException.class, () -> {
            rabbitMqSender.sendBatchMessage(null);
        });
        verify(template, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testRetry_noSuccess() {
        var retryCount = (int) ReflectionTestUtils.getField(rabbitMqSender, RabbitMqSenderImpl.class, "retryCount");
        var retryDelaySeconds = (int) ReflectionTestUtils.getField(rabbitMqSender, RabbitMqSenderImpl.class, "retryDelaySeconds");

        doThrow(new RuntimeException("Test exception"))
                .when(template).convertAndSend(anyString(), anyString(), any(Object.class));

        var scheduler = VirtualTimeScheduler.getOrSet();

        StepVerifier.withVirtualTime(() -> Mono.fromRunnable(() -> rabbitMqSender.sendBatchMessage(List.of(1))))
                .thenAwait(Duration.ofSeconds((long) retryCount * retryDelaySeconds + 2))
                .verifyComplete();

        var messages = rabbitTestUtils.drainTestQueue(RabbitMqTestConfig.TEST_QUEUE,
                new ParameterizedTypeReference<Integer>() {
                }, 1000, 0);
        assertEquals(0, messages.size());
        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {

                    verify(template, times(3)).convertAndSend(anyString(), anyString(), any(Object.class));
                });
    }

    @Test
    void testRetry_recover() {
        var retryCount = (int) ReflectionTestUtils.getField(rabbitMqSender, RabbitMqSenderImpl.class, "retryCount");
        var retryDelaySeconds = (int) ReflectionTestUtils.getField(rabbitMqSender, RabbitMqSenderImpl.class, "retryDelaySeconds");

        doThrow(new RuntimeException("Test exception"))
                .doCallRealMethod()
                .when(template).convertAndSend(anyString(), anyString(), any(Object.class));

        var scheduler = VirtualTimeScheduler.getOrSet();

        StepVerifier.withVirtualTime(() -> Mono.fromRunnable(() -> rabbitMqSender.sendBatchMessage(List.of(1))))
                .thenAwait(Duration.ofSeconds((long) retryCount * retryDelaySeconds + 2))
                .verifyComplete();

        var messages = rabbitTestUtils.drainTestQueue(RabbitMqTestConfig.TEST_QUEUE,
                new ParameterizedTypeReference<Integer>() {
                }, 1000, 1);
        assertEquals(1, messages.size());
        assertEquals(1, messages.get(0));
        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    verify(template, times(2)).convertAndSend(anyString(), anyString(), any(Object.class));
                });
    }

    @Test
    void sendMessageWithHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("key1", "value1");
        headers.put("key2", "value2");
        IdGeneratedImpl message = IdGeneratedImpl.builder().id(1L).createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
        assertDoesNotThrow(() -> rabbitMqSender.sendMessageWithHeaders(message, headers));
        var messages = rabbitTestUtils.drainTestQueueWithHeaders(RabbitMqTestConfig.TEST_QUEUE,
                new ParameterizedTypeReference<IdGeneratedImpl>() {
                }, 1000, 1);

        assertEquals(1, messages.size());
        assertEquals(message, messages.get(0).getFirst());
        assertEquals("value1", messages.get(0).getSecond().get("key1"));
        assertEquals("value2", messages.get(0).getSecond().get("key2"));
    }

    @Test
    void configureRetry() {
        rabbitMqSender.configureRetry(1, 1);
        RetryBackoffSpec retrySpec = rabbitMqSender.getRetrySpec();
        assertEquals(1, retrySpec.maxAttempts);
        assertEquals(Duration.ofSeconds(1), retrySpec.minBackoff);

    }
}
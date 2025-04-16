package com.mocicarazvan.websocketservice.listeners;

import com.mocicarazvan.websocketservice.dtos.bought.InternalBoughtBody;
import com.mocicarazvan.websocketservice.service.BoughtNotificationService;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ContextConfiguration(classes = {BoughtPlansQueListener.class})
class BoughtPlansQueListenerTest extends BaseListenerTestClass {


    @MockBean
    private BoughtNotificationService boughtNotificationService;

    @Autowired
    private RabbitProperties rabbitProperties;

    @Autowired
    private BoughtPlansQueListener boughtPlansQueListener;

    @Value("${plan.bought.queue.name}")
    private String planBoughtQueue;

    List<InternalBoughtBody.InnerPlanResponse> plans = IntStream.range(0, 4)
            .mapToObj(i -> new InternalBoughtBody.InnerPlanResponse(String.valueOf(i), "plan" + i))
            .toList();
    InternalBoughtBody internalBoughtBody = new InternalBoughtBody("sender@email.com", plans);

    @Autowired
    @Qualifier("scheduledExecutorService")
    private SimpleAsyncTaskExecutor scheduledExecutorService;


    @Test
    @Order(2)
    void loads() {
        assertNotNull(boughtPlansQueListener);
        assertNotNull(simpleRabbitListenerContainerFactory);
        Object executor = ReflectionTestUtils.getField(simpleRabbitListenerContainerFactory, "taskExecutor");
        assertNotNull(executor);
        assertSame(scheduledExecutorService, executor);

    }


    @Test
    void testListen_success() {
        doNothing().when(boughtNotificationService).saveInternalNotifications(any());
        rabbitTemplate.convertAndSend(planBoughtQueue, internalBoughtBody);
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    ArgumentCaptor<InternalBoughtBody> argumentCaptor = ArgumentCaptor.forClass(InternalBoughtBody.class);
                    verify(boughtNotificationService, times(1)).saveInternalNotifications(argumentCaptor.capture());
                    assertEquals(internalBoughtBody, argumentCaptor.getValue());
                });
        Message dlqMessage = rabbitTemplate.receive(planBoughtQueue + ".dlq", 5000);
        assertNull(dlqMessage);
    }

    @Test
    void testListen_throwsIllegalArgumentException() {
        try (LogCaptor logCaptor = LogCaptor.forClass(RejectAndDontRequeueRecoverer.class)) {
            doThrow(new IllegalArgumentException("Could not serialize plan title"))
                    .when(boughtNotificationService)
                    .saveInternalNotifications(any());
            rabbitTemplate.convertAndSend(planBoughtQueue, internalBoughtBody);
            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> {
                        verify(boughtNotificationService, times(rabbitProperties.getListener().getSimple().getRetry().getMaxAttempts())).saveInternalNotifications(any());

                        assertTrue(logCaptor.getWarnLogs().stream()
                                .anyMatch(m -> m.contains("Retries exhausted for message")));
                    });
        }

        Message dlqMessage = rabbitTemplate.receive(planBoughtQueue + ".dlq", 5000);
        assertNotNull(dlqMessage, "Expected message to be in DLQ, but none was found.");
        InternalBoughtBody body = (InternalBoughtBody) rabbitTemplate.getMessageConverter().fromMessage(dlqMessage);
        assertEquals(internalBoughtBody, body);
    }

    @Test
    void testListen_throwsIllegalArgumentExceptionRecovers() {
        try (LogCaptor logCaptor = LogCaptor.forClass(RejectAndDontRequeueRecoverer.class)) {
            doThrow(new IllegalArgumentException("Could not serialize plan title")).doNothing()
                    .when(boughtNotificationService)
                    .saveInternalNotifications(any());
            rabbitTemplate.convertAndSend(planBoughtQueue, internalBoughtBody);
            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> {
                        verify(boughtNotificationService, times(2)).saveInternalNotifications(any());

                        assertTrue(logCaptor.getWarnLogs().stream()
                                .noneMatch(m -> m.contains("Retries exhausted for message")));
                    });
        }
        Message dlqMessage = rabbitTemplate.receive(planBoughtQueue + ".dlq", 5000);
        assertNull(dlqMessage);
    }

    @Test
    void testListen_throwsValidationException() {
        InternalBoughtBody invalidBody = new InternalBoughtBody("", List.of());

        try (LogCaptor recoverCaptor = LogCaptor.forClass(RejectAndDontRequeueRecoverer.class);
             LogCaptor rejectionLogs = LogCaptor.forClass(ConditionalRejectingErrorHandler.class)) {
            rabbitTemplate.convertAndSend(planBoughtQueue, invalidBody);
            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> {
                        verify(boughtNotificationService, never()).saveInternalNotifications(any());

                        assertTrue(recoverCaptor.getWarnLogs().stream()
                                .anyMatch(m -> m.contains("Retries exhausted for message")));
                        assertTrue(rejectionLogs.getWarnLogs().stream()
                                .anyMatch(m -> m.contains("Execution of Rabbit message listener failed")));
                    });
        }
        Message dlqMessage = rabbitTemplate.receive(planBoughtQueue + ".dlq", 5000);
        assertNotNull(dlqMessage, "Expected message to be in DLQ, but none was found.");
        InternalBoughtBody body = (InternalBoughtBody) rabbitTemplate.getMessageConverter().fromMessage(dlqMessage);
        assertEquals(invalidBody, body);
    }
}
package com.mocicarazvan.websocketservice.listeners;

import com.mocicarazvan.websocketservice.dtos.bought.InternalBoughtBody;
import com.mocicarazvan.websocketservice.service.BoughtNotificationService;
import com.mocicarazvan.websocketservice.testUtils.AssertionTestUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

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
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
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

        doThrow(new IllegalArgumentException("Could not serialize plan title"))
                .when(boughtNotificationService)
                .saveInternalNotifications(any());
        rabbitTemplate.convertAndSend(planBoughtQueue, internalBoughtBody);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(() -> {
                    verify(boughtNotificationService, times(rabbitProperties.getListener().getSimple().getRetry().getMaxAttempts())).saveInternalNotifications(any());

                });


        Message dlqMessage = rabbitTemplate.receive(planBoughtQueue + ".dlq", 5000);
        assertNotNull(dlqMessage, "Expected message to be in DLQ, but none was found.");
        InternalBoughtBody body = (InternalBoughtBody) rabbitTemplate.getMessageConverter().fromMessage(dlqMessage);
        assertEquals(internalBoughtBody, body);
    }

    @Test
    void testListen_throwsIllegalArgumentExceptionRecovers() {
        doThrow(new IllegalArgumentException("Could not serialize plan title")).doNothing()
                .when(boughtNotificationService)
                .saveInternalNotifications(any());
        rabbitTemplate.convertAndSend(planBoughtQueue, internalBoughtBody);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(() -> {
                    verify(boughtNotificationService, times(2)).saveInternalNotifications(any());

                });

        Message dlqMessage = rabbitTemplate.receive(planBoughtQueue + ".dlq", 5000);
        assertNull(dlqMessage);
    }

    @Test
    void testListen_throwsValidationException() {
        InternalBoughtBody invalidBody = new InternalBoughtBody("", List.of());


        rabbitTemplate.convertAndSend(planBoughtQueue, invalidBody);
        await().atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(() -> {
                    verify(boughtNotificationService, never()).saveInternalNotifications(any());


                });

        Message dlqMessage = rabbitTemplate.receive(planBoughtQueue + ".dlq", 5000);
        assertNotNull(dlqMessage, "Expected message to be in DLQ, but none was found.");
        InternalBoughtBody body = (InternalBoughtBody) rabbitTemplate.getMessageConverter().fromMessage(dlqMessage);
        assertEquals(invalidBody, body);
    }
}
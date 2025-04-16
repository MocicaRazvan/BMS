package com.mocicarazvan.websocketservice.listeners;

import com.mocicarazvan.websocketservice.dtos.generic.ApproveNotificationBody;
import com.mocicarazvan.websocketservice.dtos.generic.ApproveResponse;
import com.mocicarazvan.websocketservice.dtos.generic.NotificationTemplateResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.models.generic.ApprovedModel;
import com.mocicarazvan.websocketservice.service.generic.ApproveNotificationServiceTemplate;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class BaseApproveListenerTest<R extends ApprovedModel, RRESP extends ApproveResponse, BODY extends ApproveNotificationBody,
        RESPONSE extends NotificationTemplateResponse<RRESP, ApprovedNotificationType>> extends BaseListenerTestClass {

    @Autowired
    @Qualifier("scheduledExecutorService")
    private SimpleAsyncTaskExecutor scheduledExecutorService;

    private ApproveNotificationServiceTemplate<R, RRESP, BODY, RESPONSE> serviceTemplate;
    private String queueName;
    private final Class<BODY> bodyClass;

    protected BaseApproveListenerTest(Class<BODY> bodyClass) {
        this.bodyClass = bodyClass;
    }


    @Test
    @Order(2)
    void loads() {
        assertNotNull(simpleRabbitListenerContainerFactory);
        Object executor = ReflectionTestUtils.getField(simpleRabbitListenerContainerFactory, "taskExecutor");
        assertNotNull(executor);
        assertSame(scheduledExecutorService, executor);

    }

    protected abstract BODY createBody();

    protected abstract BODY createInvalidBody();

    protected abstract RESPONSE createResponse();

    protected void setServiceTemplate(ApproveNotificationServiceTemplate<R, RRESP, BODY, RESPONSE> serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
    }

    protected void setQueueName(String queueName) {
        this.queueName = queueName;
    }


    @Test
    void testListen_success() {
        BODY body = createBody();
        RESPONSE response = createResponse();
        when(serviceTemplate.saveApprovedNotificationCreateReference(body, body.getReferenceId()))
                .thenReturn(response);
        rabbitTemplate.convertAndSend(queueName, body);
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    ArgumentCaptor<BODY> argumentCaptor = ArgumentCaptor.forClass(bodyClass);
                    verify(serviceTemplate, times(1)).saveApprovedNotificationCreateReference(argumentCaptor.capture(), any());
                    assertEquals(body, argumentCaptor.getValue());
                });
        Message dlqMessage = rabbitTemplate.receive(queueName + ".dlq", 5000);
        assertNull(dlqMessage);
    }

    @Test
    void testListenReferenceIdNull_throwsIllegalArgumentException() {
        var invalidBody = createInvalidBody();
        try (LogCaptor logCaptor = LogCaptor.forClass(RejectAndDontRequeueRecoverer.class)) {
            rabbitTemplate.convertAndSend(queueName, invalidBody);
            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> {
                        verify(serviceTemplate, never()).saveApprovedNotificationCreateReference(any(), any());

                        assertTrue(logCaptor.getWarnLogs().stream()
                                .anyMatch(m -> m.contains("Retries exhausted for message")));
                    });
            Message dlqMessage = rabbitTemplate.receive(queueName + ".dlq", 5000);
            assertNotNull(dlqMessage, "Expected message to be in DLQ, but none was found.");
            BODY body = (BODY) rabbitTemplate.getMessageConverter().fromMessage(dlqMessage);
            assertEquals(invalidBody, body);
        }
    }
}

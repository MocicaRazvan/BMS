package com.mocicarazvan.templatemodule.services;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.config.RabbitMqTestConfig;
import com.mocicarazvan.templatemodule.config.TestContainerImages;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.dtos.notifications.ApproveNotificationBody;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.enums.ApprovedNotificationType;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqApprovedSenderImpl;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqSenderImpl;
import com.mocicarazvan.templatemodule.testUtils.AssertionTestUtils;
import com.mocicarazvan.templatemodule.testUtils.RabbitTestUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import java.time.LocalDateTime;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(RabbitMqTestConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RabbitMqApprovedSenderTest {
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

    private RabbitMqApprovedSender<ApproveDto> rabbitMqApprovedSender;
    private RabbitMqSender rabbitMqSender;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    UserDto authUser = UserDto.builder()
            .id(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .email("email")
            .build();
    UserDto modelUser = UserDto.builder()
            .id(2L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .email("modelEmail")
            .build();
    ApproveDto approveDto = ApproveDto.builder().
            id(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .title("title")
            .body("body")
            .build();

    ResponseWithUserDto<ApproveDto> re = new ResponseWithUserDto<>(approveDto, modelUser);

    @BeforeEach
    void setup() {
        rabbitMqSender = new RabbitMqSenderImpl(RabbitMqTestConfig.TEST_EXCHANGE,
                RabbitMqTestConfig.TEST_ROUTING_KEY, template, 10);
        rabbitMqApprovedSender = new RabbitMqApprovedSenderImpl<>("extraLink", rabbitMqSender);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SneakyThrows
    void sendMessage(boolean app) {
        assertDoesNotThrow(() -> rabbitMqApprovedSender.sendMessage(app, re, authUser));
        var message = rabbitTestUtils.drainTestQueue(RabbitMqTestConfig.TEST_QUEUE,
                new ParameterizedTypeReference<ApproveNotificationBody>() {
                }, 4000, 1);
        assertEquals(1, message.size());
        var notificationBody = message.get(0);
        assertEquals(authUser.getEmail(), notificationBody.getSenderEmail());
        assertEquals(modelUser.getEmail(), notificationBody.getReceiverEmail());
        assertEquals("extraLink" + approveDto.getId(), notificationBody.getExtraLink());
        var approvedEnum = app ? ApprovedNotificationType.APPROVED : ApprovedNotificationType.DISAPPROVED;
        assertEquals(approvedEnum, notificationBody.getType());
        var content = objectMapper.readValue(notificationBody.getContent(), RabbitMqApprovedSenderImpl.RMQContent.class);
        assertEquals(approveDto.getTitle(), content.getTitle());

    }

    @Test
    @SneakyThrows
    void sendMessage_serError() {
        var spyMapper = spy(new ObjectMapper());
        ReflectionTestUtils.setField(rabbitMqApprovedSender, "objectMapper", spyMapper);
        doThrow(new JsonMappingException(null, "test")).when(spyMapper).writeValueAsString(any());

        rabbitMqApprovedSender.sendMessage(true, re, authUser);

        await()
                .atMost(AssertionTestUtils.AWAiTILITY_TIMEOUT_SECONDS)
                .untilAsserted(() -> {
                    verify(spyMapper, times(1)).writeValueAsString(any());
                });
    }

}
package com.mocicarazvan.templatemodule.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.dtos.notifications.ApproveNotificationBody;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.enums.ApprovedNotificationType;
import com.mocicarazvan.templatemodule.services.RabbitMqApprovedSender;
import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.utils.MonoWrapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Builder
@Getter
public class RabbitMqApprovedSenderImpl<T extends ApproveDto> implements RabbitMqApprovedSender<T> {

    private final String extraLink;
    private final RabbitMqSender rabbitMqSender;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void sendMessage(boolean approved, ResponseWithUserDto<T> model, UserDto authUser) {
        MonoWrapper.wrapBlockingFunction(() -> {
            try {

                rabbitMqSender.sendMessage(
                        ApproveNotificationBody.builder()
                                .senderEmail(authUser.getEmail())
                                .receiverEmail(model.getUser().getEmail())
                                .type(approved ? ApprovedNotificationType.APPROVED : ApprovedNotificationType.DISAPPROVED)
                                .referenceId(model.getModel().getId())
                                .content(
                                        objectMapper.writeValueAsString(
                                                RMQContent.builder()
                                                        .title(model.getModel().getTitle())
                                                        .build()
                                        )
                                )
                                .extraLink(extraLink + model.getModel().getId())
                                .build()
                );
            } catch (JsonProcessingException e) {
                log.error("Error sending message to rabbitmq: {}", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error sending message to rabbitmq", e);
            }
        }, rabbitMqSender.getRetrySpec());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RMQContent {
        private String title;
    }
}

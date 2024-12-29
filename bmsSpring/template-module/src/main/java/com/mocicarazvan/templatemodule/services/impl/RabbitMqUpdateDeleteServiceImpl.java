package com.mocicarazvan.templatemodule.services.impl;

import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Builder
public class RabbitMqUpdateDeleteServiceImpl<T> implements RabbitMqUpdateDeleteService<T> {
    private final RabbitMqSender updateSender;
    private final RabbitMqSender deleteSender;

    @Override
    public void sendUpdateMessage(T model) {
        updateSender.sendMessage(model);
    }

    @Override
    public void sendDeleteMessage(T model) {
        deleteSender.sendMessage(model);
    }

    @Override
    public void sendBatchUpdateMessage(List<T> model) {
        updateSender.sendBatchMessage(model);
    }

    @Override
    public void sendBatchDeleteMessage(List<T> model) {
        deleteSender.sendBatchMessage(model);
    }
}

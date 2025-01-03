package com.mocicarazvan.templatemodule.services.impl;

import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Builder
public class RabbitMqUpdateDeleteServiceImpl<T> implements RabbitMqUpdateDeleteService<T> {
    private final RabbitMqSender updateSender;
    private final RabbitMqSender deleteSender;

    @Override
    public void sendUpdateMessage(@NonNull T model) {
        updateSender.sendMessage(model);
    }

    @Override
    public void sendDeleteMessage(@NonNull T model) {
        deleteSender.sendMessage(model);
    }

    @Override
    public void sendBatchUpdateMessage(@NonNull List<T> model) {
        updateSender.sendBatchMessage(model);
    }

    @Override
    public void sendBatchDeleteMessage(@NonNull List<T> model) {
        deleteSender.sendBatchMessage(model);
    }
}

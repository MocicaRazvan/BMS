package com.mocicarazvan.templatemodule.services.impl;

import com.mocicarazvan.templatemodule.services.RabbitMqSender;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Builder
@Getter
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
    public void sendBatchUpdateMessage(@NonNull List<T> models) {
        if (models.isEmpty()) {
            return;
        }
        updateSender.sendBatchMessage(models);
    }

    @Override
    public void sendBatchDeleteMessage(@NonNull List<T> models) {
        if (models.isEmpty()) {
            return;
        }
        deleteSender.sendBatchMessage(models);
    }
}

package com.mocicarazvan.templatemodule.services.impl;

import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;

import java.util.List;

public class RabbitMqUpdateDeleteNoOpServiceImpl<T> implements RabbitMqUpdateDeleteService<T> {


    @Override
    public void sendUpdateMessage(T model) {

    }

    @Override
    public void sendDeleteMessage(T model) {

    }

    @Override
    public void sendBatchUpdateMessage(List<T> models) {

    }

    @Override
    public void sendBatchDeleteMessage(List<T> models) {

    }
}

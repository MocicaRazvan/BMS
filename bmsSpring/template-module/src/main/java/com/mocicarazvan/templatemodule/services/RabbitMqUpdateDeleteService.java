package com.mocicarazvan.templatemodule.services;


import java.util.List;

public interface RabbitMqUpdateDeleteService<T> {

    void sendUpdateMessage(T model);

    void sendDeleteMessage(T model);

    void sendBatchUpdateMessage(List<T> model);

    void sendBatchDeleteMessage(List<T> model);
}

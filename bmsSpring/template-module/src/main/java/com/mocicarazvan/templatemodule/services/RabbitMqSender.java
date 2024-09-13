package com.mocicarazvan.templatemodule.services;

public interface RabbitMqSender {

    <T> void sendMessage(T message);
}

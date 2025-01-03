package com.mocicarazvan.archiveservice.services;

public interface ContainerNotify {
    void notifyContainersStartCron(String queueName);

    void notifyContainersStartManual(String queueName);

    void notifyContainersStop(String queueName);
}

package com.mocicarazvan.archiveservice.services;

import java.util.List;

public interface BatchNotify {
    <T> void notifyBatchUpdate(List<T> items, String queueName);


}

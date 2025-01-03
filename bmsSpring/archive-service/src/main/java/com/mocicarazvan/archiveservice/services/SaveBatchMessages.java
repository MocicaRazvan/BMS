package com.mocicarazvan.archiveservice.services;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface SaveBatchMessages {
    <T> void saveBatch(List<T> items, String queueName) throws IOException;
}

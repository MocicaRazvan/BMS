package com.mocicarazvan.archiveservice.services;

import java.io.IOException;
import java.util.List;

public interface SaveBatchMessages {
    <T> void saveBatch(List<T> items, String name) throws IOException;
}

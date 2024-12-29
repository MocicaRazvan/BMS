package com.mocicarazvan.archiveservice.services;

import java.io.IOException;
import java.util.List;

public interface DirService {
    <T> void saveBatchToDisk(List<T> items, String name) throws IOException;
}

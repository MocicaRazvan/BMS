package com.mocicarazvan.archiveservice.services.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.mocicarazvan.archiveservice.services.SaveBatchMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
// todo implement this well its just a placeholder
public class DirService implements SaveBatchMessages {

    private final Scheduler derSerScheduler;

    public static final String ROOT_DIR_PATH = "archive/data".replace("/", File.separator);
    private final ObjectMapper objectMapper;

    public DirService(ObjectMapper objectMapper, @Qualifier("parallelScheduler") Scheduler derSerScheduler) {
        this.objectMapper = objectMapper;
        this.derSerScheduler = derSerScheduler;
    }

    public File createRootDirIfNotExists() {
        File rootDir = new File(ROOT_DIR_PATH);
        if (!rootDir.exists() && rootDir.mkdirs()) {
            log.info("Root directory created: {}", rootDir.getAbsolutePath());
        }
        return rootDir;
    }

    public File createDirIfNotExists(File rootDir, String dirName) {
        File dir = new File(rootDir, dirName);
        if (!dir.exists() && dir.mkdirs()) {
            log.info("Directory created: {}", dir.getAbsolutePath());
        }
        return dir;
    }

    @Override
    public <T> void saveBatch(List<T> items, String queueName) {
        Mono.fromCallable(() -> {
                    File dir = createDirIfNotExists(createRootDirIfNotExists(), queueName);
                    File batchFile = new File(dir, getCurrentTime() + "_" + UUID.randomUUID() + ".json");

                    // the list can be very big
//                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(batchFile, items);
                    //streaming
                    try (SequenceWriter writer = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValuesAsArray(batchFile)) {
                        for (T item : items) {
                            writer.write(item);
                        }
                    }

                    return true;
                })
                .onErrorResume(e -> {
                    log.error("Error saving batch: {}", e.getMessage());
                    return Mono.just(false);
                })
//                .subscribeOn(derSerScheduler)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public String getCurrentTime() {
        return ZonedDateTime.now(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));

    }
}

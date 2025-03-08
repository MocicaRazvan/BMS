package com.mocicarazvan.fileservice.config;

import com.mocicarazvan.fileservice.service.MediaService;
import com.mocicarazvan.fileservice.utils.CronUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TasksConfig {

    private final MediaService mediaService;

    @Value("${media.clear-old-media.cron:0 0 0 * * *}")
    private String clearOldMediaCron;


    @Value("${media.clear-old-media.enabled:true}")
    private boolean isClearOldMediaEnabled;

    @Bean
    public Disposable clearOldMedia() {
        if (!isClearOldMediaEnabled) {
            log.info("Clear old media is disabled");
            return Flux.never().subscribe();
        }
        Pair<Duration, Duration> durations = CronUtils.computeDurations(clearOldMediaCron, LocalDateTime.now());
        return Flux.interval(durations.getFirst(), durations.getSecond())
                .concatMap(_ -> {
                    log.info("Clearing old carts");
                    return mediaService.hardDeleteFiles();
                })
                .subscribe(
                        result -> log.info("Clear old media result: {}", result),
                        error -> log.error("Error clearing old media", error),
                        () -> log.info("Clear old media completed")
                );
    }
}

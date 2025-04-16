package com.mocicarazvan.archiveservice.config;


import com.mocicarazvan.archiveservice.repositories.ContainerNotifyRepository;
import com.mocicarazvan.archiveservice.utils.CronUtils;
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
    private final ContainerNotifyRepository containerNotifyRepository;

    @Value("${cart.clear-old-archive.cron:0 0 */2 * * *}")
    private String clearOldArchiveCron;

    @Value("${cart.clear-old-archive.enabled:true}")
    private boolean isClearEnabled;


    @Bean
    public Disposable clearOldArchive() {

        if (!isClearEnabled) {
            log.info("Clear old archive task is disabled");
            return Flux.never().subscribe();
        }

        Pair<Duration, Duration> durations = CronUtils.computeDurations(clearOldArchiveCron, LocalDateTime.now());
        return Flux.interval(durations.getFirst(), durations.getSecond())
                .concatMap(_ -> {
                    log.info("Clearing old archive");
                    return
                            containerNotifyRepository.invalidateNotifications();
                })
                .subscribe(
                        result -> log.info("Old archive cleared: {}", result),
                        error -> log.error("Error clearing old archive", error),
                        () -> log.info("Clearing old archive task completed")
                );
    }

}

package com.mocicarazvan.archiveservice;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class ArchiveServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchiveServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info("Default timezone set to: {}", TimeZone.getDefault().getID());

        // Log current time in UTC
        ZonedDateTime utcTime = ZonedDateTime.now(ZoneId.of("UTC"));
        log.info("Current UTC time: {}", utcTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));

        // Log current time in server's default timezone
        ZonedDateTime serverTime = ZonedDateTime.now();
        log.info("Current server local time: {}", serverTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
    }


}

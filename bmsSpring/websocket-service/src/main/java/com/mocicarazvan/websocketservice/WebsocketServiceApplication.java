package com.mocicarazvan.websocketservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import reactor.core.publisher.Hooks;

import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableTransactionManagement
@EnableRetry
public class WebsocketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Hooks.enableAutomaticContextPropagation();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    }
}

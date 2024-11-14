package com.mocicarazvan.gatewayservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import reactor.core.publisher.Hooks;

import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient

public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Hooks.enableAutomaticContextPropagation();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    }
}

package com.mocicarazvan.planservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import reactor.core.publisher.Hooks;

import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class PlanServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Hooks.enableAutomaticContextPropagation();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    }
}

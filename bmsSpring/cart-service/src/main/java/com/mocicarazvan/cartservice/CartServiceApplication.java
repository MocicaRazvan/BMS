package com.mocicarazvan.cartservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.hateoas.config.EnableHypermediaSupport;

import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@ComponentScan(basePackages = {"com.mocicarazvan.*"})

public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Hooks.enableAutomaticContextPropagation();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    }
}

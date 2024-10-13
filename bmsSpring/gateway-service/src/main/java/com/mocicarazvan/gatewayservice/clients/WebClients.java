package com.mocicarazvan.gatewayservice.clients;


import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClients {


    @Bean
    @Profile("!k8s")
    @LoadBalanced
    public WebClient.Builder webClient() {
        return WebClient.builder();
    }

    @Bean
    @Profile("k8s")
    public WebClient.Builder webClientK8s() {
        return WebClient.builder();
    }

}

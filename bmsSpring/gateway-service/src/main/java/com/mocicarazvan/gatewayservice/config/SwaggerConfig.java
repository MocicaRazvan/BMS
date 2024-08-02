package com.mocicarazvan.gatewayservice.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("k8s")
@RequiredArgsConstructor
public class SwaggerConfig {
    @Value("${swagger.host}")
    private String swaggerHost;

    @Value("${swagger.base-path}")
    private String swaggerBasePath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url(swaggerHost + swaggerBasePath)));
    }
}

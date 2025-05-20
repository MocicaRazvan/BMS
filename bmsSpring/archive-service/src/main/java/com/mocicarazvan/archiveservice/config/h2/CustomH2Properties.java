package com.mocicarazvan.archiveservice.config.h2;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.custom.h2")
@Component
@Data
public class CustomH2Properties {
    private String serverHost;
}

package com.mocicarazvan.gatewayservice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "next.auth")
public class NextAuthProperties {
    private String secret;
    private boolean csrfEnabled;
    private List<String> csrfExemptedUrls;

    public void setCsrfExemptedUrl(String urls) {
        csrfExemptedUrls = List.of(urls.split(","));
    }
}

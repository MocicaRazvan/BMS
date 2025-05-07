package com.mocicarazvan.websocketservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@ConfigurationProperties(prefix = "spring.encryption")
@Data
public class EncryptionProperties {
    private String secretKey; // Base64 encoded secret key

    public byte[] getSecretKeyBytes() {
        return Base64.getDecoder()
                .decode(secretKey);
    }
}

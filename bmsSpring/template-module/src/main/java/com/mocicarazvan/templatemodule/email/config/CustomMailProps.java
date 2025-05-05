package com.mocicarazvan.templatemodule.email.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

//@Component
//@ConfigurationProperties(prefix = "spring.mail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class CustomMailProps {
    private String host;
    private int port;
    private String username;
    private String password;
    private int cacherSeconds = 60 * 60 * 4;
    private String cacherPrefix = "mx:";
    private final Map<String, String> properties = new HashMap<>();

    public Duration getCacherSecondsDuration() {
        return Duration.ofSeconds(cacherSeconds);
    }
}

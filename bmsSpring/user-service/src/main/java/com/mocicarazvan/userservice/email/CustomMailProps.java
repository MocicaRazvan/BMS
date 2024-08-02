package com.mocicarazvan.userservice.email;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@EqualsAndHashCode(callSuper = true)
@Component
@ConfigurationProperties(prefix = "spring.mail")
@Data
public class CustomMailProps extends com.mocicarazvan.templatemodule.email.config.CustomMailProps {
}

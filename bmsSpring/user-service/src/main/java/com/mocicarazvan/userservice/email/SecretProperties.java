package com.mocicarazvan.userservice.email;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@EqualsAndHashCode(callSuper = true)
@Component
@ConfigurationProperties(prefix = "encoding")
@PropertySource("classpath:secret.properties")
@Data
public class SecretProperties extends com.mocicarazvan.templatemodule.email.config.SecretProperties {
}

package com.mocicarazvan.templatemodule.email.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Component
//@ConfigurationProperties(prefix = "encoding")
//@PropertySource("classpath:secret.properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class SecretProperties {
    private String secret;
    private String springMailPassword;
}

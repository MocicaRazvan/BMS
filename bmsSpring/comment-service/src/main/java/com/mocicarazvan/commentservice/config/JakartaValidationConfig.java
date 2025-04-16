package com.mocicarazvan.commentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class JakartaValidationConfig {
    @Bean
    public LocalValidatorFactoryBean jakartaLocalValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
}

package com.mocicarazvan.cartservice.config;

import com.mocicarazvan.cartservice.models.UserCart;
import com.mocicarazvan.templatemodule.dbCallbacks.IdGeneratedBeforeSaveCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback;

@Configuration
public class DbConfig {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<UserCart> userCartBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }
}

package com.mocicarazvan.postservice.config;


import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.templatemodule.dbCallbacks.TitleBodyImagesBeforeSaveCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback;

@Configuration
public class DbConfig {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<Post> postBeforeSaveCallback() {
        return new TitleBodyImagesBeforeSaveCallback<>();
    }
}

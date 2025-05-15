package com.mocicarazvan.postservice.config;


import com.mocicarazvan.ollamasearch.dbCallbacks.EmbedModelBeforeSaveCallback;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.postservice.models.PostEmbedding;
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

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<PostEmbedding> postEmbeddingBeforeSaveCallback() {
        return new EmbedModelBeforeSaveCallback<>();
    }
}

package com.mocicarazvan.recipeservice.config;

import com.mocicarazvan.ollamasearch.dbCallbacks.EmbedModelBeforeSaveCallback;
import com.mocicarazvan.recipeservice.convertors.DietTypeReadingConvertor;
import com.mocicarazvan.recipeservice.convertors.DietTypeWritingConvertor;
import com.mocicarazvan.recipeservice.models.IngredientQuantity;
import com.mocicarazvan.recipeservice.models.Recipe;
import com.mocicarazvan.recipeservice.models.RecipeEmbedding;
import com.mocicarazvan.templatemodule.dbCallbacks.IdGeneratedBeforeSaveCallback;
import com.mocicarazvan.templatemodule.dbCallbacks.TitleBodyImagesBeforeSaveCallback;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback;

@Configuration
public class DbConfig {

    @Bean
    public R2dbcCustomConversions getCustomConverters(ConnectionFactory connectionFactory) {
        return R2dbcCustomConversions.of(DialectResolver.getDialect(connectionFactory),
                new DietTypeReadingConvertor(),
                new DietTypeWritingConvertor()
        );
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<Recipe> recipeBeforeSaveCallback() {
        return new TitleBodyImagesBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<IngredientQuantity> ingredientQuantityBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<RecipeEmbedding> recipeEmbeddingBeforeSaveCallback() {
        return new EmbedModelBeforeSaveCallback<>();
    }
}

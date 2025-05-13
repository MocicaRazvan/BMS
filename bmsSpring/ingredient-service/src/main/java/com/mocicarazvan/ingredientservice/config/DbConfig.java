package com.mocicarazvan.ingredientservice.config;

import com.mocicarazvan.ingredientservice.convertors.DietTypeReadingConvertor;
import com.mocicarazvan.ingredientservice.convertors.DietTypeWritingConvertor;
import com.mocicarazvan.ingredientservice.convertors.UnitTypeReadingConvertor;
import com.mocicarazvan.ingredientservice.convertors.UnitTypeWritingConvertor;
import com.mocicarazvan.ingredientservice.models.Ingredient;
import com.mocicarazvan.ingredientservice.models.NutritionalFact;
import com.mocicarazvan.templatemodule.dbCallbacks.IdGeneratedBeforeSaveCallback;
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
                new DietTypeWritingConvertor(),
                new UnitTypeReadingConvertor(),
                new UnitTypeWritingConvertor()
        );
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<Ingredient> ingredientBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<NutritionalFact> nutritionalFactBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }


}

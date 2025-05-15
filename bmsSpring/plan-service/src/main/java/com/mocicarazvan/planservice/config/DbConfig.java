package com.mocicarazvan.planservice.config;

import com.mocicarazvan.ollamasearch.dbCallbacks.EmbedModelBeforeSaveCallback;
import com.mocicarazvan.planservice.convertors.DietTypeReadingConvertor;
import com.mocicarazvan.planservice.convertors.DietTypeWritingConvertor;
import com.mocicarazvan.planservice.convertors.ObjectiveTypeReadingConvertor;
import com.mocicarazvan.planservice.convertors.ObjectiveTypeWritingConvertor;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.planservice.models.PlanEmbedding;
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
                new DietTypeWritingConvertor(),
                new ObjectiveTypeReadingConvertor(),
                new ObjectiveTypeWritingConvertor()
        );
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<Plan> planBeforeSaveCallback() {
        return new TitleBodyImagesBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<PlanEmbedding> planEmbeddingBeforeSaveCallback() {
        return new EmbedModelBeforeSaveCallback<>();
    }
}

package com.mocicarazvan.orderservice.config;


import com.mocicarazvan.orderservice.convertors.DietTypeReadingConvertor;
import com.mocicarazvan.orderservice.convertors.DietTypeWritingConvertor;
import com.mocicarazvan.orderservice.convertors.ObjectiveTypeReadingConvertor;
import com.mocicarazvan.orderservice.convertors.ObjectiveTypeWritingConvertor;
import com.mocicarazvan.orderservice.models.CustomAddress;
import com.mocicarazvan.orderservice.models.PlanOrder;
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
                new ObjectiveTypeReadingConvertor(),
                new ObjectiveTypeWritingConvertor(),
                new DietTypeWritingConvertor(),
                new DietTypeReadingConvertor()
        );
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<CustomAddress> customAddressBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<com.mocicarazvan.orderservice.models.Order> orderBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<PlanOrder> planOrderBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

}

package com.mocicarazvan.dayservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.dayservice.convertors.DayTypeReadingConvertor;
import com.mocicarazvan.dayservice.convertors.DayTypeWritingConvertor;
import com.mocicarazvan.dayservice.convertors.StringToJsonNodeConverter;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.dayservice.models.DayCalendar;
import com.mocicarazvan.dayservice.models.Meal;
import com.mocicarazvan.templatemodule.dbCallbacks.IdGeneratedBeforeSaveCallback;
import com.mocicarazvan.templatemodule.dbCallbacks.TitleBodyBeforeSaveCallback;
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
    public R2dbcCustomConversions getCustomConverters(ConnectionFactory connectionFactory
    ) {
        return R2dbcCustomConversions.of(DialectResolver.getDialect(connectionFactory),
                new DayTypeReadingConvertor(),
                new DayTypeWritingConvertor(),
                new StringToJsonNodeConverter(new ObjectMapper())
        );
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<Day> dayBeforeSaveCallback() {
        return new TitleBodyBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<Meal> mealBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<DayCalendar> dayCalendarBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

}

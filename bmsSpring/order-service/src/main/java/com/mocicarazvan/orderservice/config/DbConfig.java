package com.mocicarazvan.orderservice.config;


import com.mocicarazvan.orderservice.convertors.DietTypeReadingConvertor;
import com.mocicarazvan.orderservice.convertors.DietTypeWritingConvertor;
import com.mocicarazvan.orderservice.convertors.ObjectiveTypeReadingConvertor;
import com.mocicarazvan.orderservice.convertors.ObjectiveTypeWritingConvertor;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;

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


}

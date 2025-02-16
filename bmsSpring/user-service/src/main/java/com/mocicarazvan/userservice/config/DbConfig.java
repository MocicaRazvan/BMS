package com.mocicarazvan.userservice.config;

import com.mocicarazvan.userservice.convertors.*;
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
                new RoleReadingConvertor(),
                new RoleWritingConvertor(),
                new AuthProviderReadingConvertor(),
                new AuthProviderWritingConvertor(),
                new OTPTypeReadingConvertor(),
                new OTPTypeWritingConverter()
        );
    }


}

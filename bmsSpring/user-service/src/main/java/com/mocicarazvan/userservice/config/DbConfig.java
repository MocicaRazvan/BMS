package com.mocicarazvan.userservice.config;

import com.mocicarazvan.templatemodule.dbCallbacks.IdGeneratedBeforeSaveCallback;
import com.mocicarazvan.userservice.convertors.*;
import com.mocicarazvan.userservice.dbCallbacks.UserCustomBeforeSaveCallback;
import com.mocicarazvan.userservice.models.JwtToken;
import com.mocicarazvan.userservice.models.OTPToken;
import com.mocicarazvan.userservice.models.OauthState;
import com.mocicarazvan.userservice.models.UserCustom;
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
                new RoleReadingConvertor(),
                new RoleWritingConvertor(),
                new AuthProviderReadingConvertor(),
                new AuthProviderWritingConvertor(),
                new OTPTypeReadingConvertor(),
                new OTPTypeWritingConverter()
        );
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<JwtToken> jwtTokenBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<OauthState> oauthStateBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<OTPToken> otpTokenBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<UserCustom> userCustomBeforeSaveCallback() {
        return new UserCustomBeforeSaveCallback();
    }

}

package com.mocicarazvan.commentservice.config;

import com.mocicarazvan.commentservice.convertors.CommentReferenceTypeReadingConvertor;
import com.mocicarazvan.commentservice.convertors.CommentReferenceTypeWritingConvertor;
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
                new CommentReferenceTypeReadingConvertor(),
                new CommentReferenceTypeWritingConvertor()
        );
    }


}

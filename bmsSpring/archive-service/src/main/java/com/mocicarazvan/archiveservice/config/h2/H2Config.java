package com.mocicarazvan.archiveservice.config.h2;

import com.mocicarazvan.archiveservice.converters.ContainerActionReadingConverter;
import com.mocicarazvan.archiveservice.converters.ContainerActionWritingConverter;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.h2.H2ConnectionOption;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.r2dbc.core.DatabaseClient;


@Configuration
@Slf4j
@RequiredArgsConstructor
public class H2Config {

    private final CustomH2Properties customH2Properties;


    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return new H2ConnectionFactory(H2ConnectionConfiguration.builder()
                .tcp(customH2Properties.getServerHost(), 9092, "app")
                .property(H2ConnectionOption.DB_CLOSE_DELAY, "-1")
                .build());
    }

    @Bean
    public R2dbcCustomConversions getCustomConverters(ConnectionFactory connectionFactory) {
        return R2dbcCustomConversions.of(DialectResolver.getDialect(connectionFactory),
                new ContainerActionReadingConverter(),
                new ContainerActionWritingConverter()
        );
    }

    @Bean
    @Primary
    public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
        return DatabaseClient.create(connectionFactory);
    }


}

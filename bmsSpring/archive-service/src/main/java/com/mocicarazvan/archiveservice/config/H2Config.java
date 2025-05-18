package com.mocicarazvan.archiveservice.config;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.h2.H2ConnectionOption;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.r2dbc.spi.*;

//spring.r2dbc.url=r2dbc:h2:file:///data/h2db/app;FILE_LOCK=SOCKET;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9092
@Configuration
public class H2Config {

    @Bean
    public ConnectionFactory connectionFactory() {
        return new H2ConnectionFactory(H2ConnectionConfiguration.builder()
                .file("./archive/data/database/h2/app")
                .username("sa")
                .password("")
                .property(H2ConnectionOption.DB_CLOSE_DELAY, "-1")
                .property(H2ConnectionOption.FILE_LOCK, "SOCKET")
                .property(H2ConnectionOption.AUTO_SERVER, "TRUE")
                .property(H2ConnectionOption.AUTO_SERVER_PORT, "9092")
                .build());
    }
}

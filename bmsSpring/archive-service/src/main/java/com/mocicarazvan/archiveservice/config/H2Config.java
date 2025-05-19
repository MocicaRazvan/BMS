package com.mocicarazvan.archiveservice.config;

import com.mocicarazvan.archiveservice.converters.ContainerActionReadingConverter;
import com.mocicarazvan.archiveservice.converters.ContainerActionWritingConverter;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.h2.H2ConnectionOption;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.r2dbc.core.DatabaseClient;

import java.io.File;
import java.net.InetAddress;


@Configuration
@Slf4j
public class H2Config {

    @Value("${spring.custom.h2.server-host}")
    private String serverHost;

    private Server tcpServer;

    @PostConstruct
    public void maybeStartH2Server() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String localIp = InetAddress.getLocalHost().getHostAddress();
            log.info("Detected hostname: {}", hostname);
            log.info("Detected local IP: {}", localIp);
            log.info("Configured H2 master host: {}", serverHost);


            boolean shouldStart = localIp.equals(serverHost) || serverHost.contains(hostname);


            if (shouldStart) {
                String rootDirPath = "archive/data".replace("/", File.separator);
                String dbDirPath = rootDirPath + File.separator + "database" + File.separator + "h2";
                File dbDir = new File(dbDirPath);

                tcpServer = Server.createTcpServer(
                        "-tcp", "-tcpAllowOthers", "-tcpPort", "9092", "-baseDir", dbDir.getAbsolutePath(), "-ifNotExists"
                ).start();
                log.info("Embedded H2 TCP server started on port 9092");
            } else {
                log.info("Skipping H2 TCP server start. Connecting as client.");
            }
        } catch (Exception e) {
            log.error("Failed to start embedded H2 TCP server", e);
        }
    }

    @PreDestroy
    public void stopH2Server() {
        if (tcpServer != null) {
            log.info("Shutting down embedded H2 TCP server");
            tcpServer.stop();
        }
    }


    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return new H2ConnectionFactory(H2ConnectionConfiguration.builder()
                .tcp(serverHost, 9092, "app")
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

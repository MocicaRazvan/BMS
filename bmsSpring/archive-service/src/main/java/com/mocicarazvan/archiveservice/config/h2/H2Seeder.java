package com.mocicarazvan.archiveservice.config.h2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class H2Seeder implements ApplicationListener<ApplicationReadyEvent> {

    private final DatabaseClient databaseClient;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        databaseClient.sql("""
                                                 CREATE TABLE IF NOT EXISTS notify_container (
                                                          id         VARCHAR(36) PRIMARY KEY,
                                                          queue_name VARCHAR(255) NOT NULL,
                                                          timestamp  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                          action     VARCHAR(50)  NOT NULL
                        
                                                        )
                        """).then().doOnSuccess(_ -> log.info("Created new notify_container table"))
                .doOnError(e -> log.error("Failed to create notify_container table", e))
                .block();

        databaseClient.sql("""
                        CREATE INDEX IF NOT EXISTS idx_notify_container_ts
                          ON notify_container(timestamp)
                        """).then().doOnSuccess(_ -> log.info("Created new index on notify_container table"))
                .doOnError(e -> log.error("Failed to create index on notify_container table", e))
                .block();

        databaseClient.sql("""
                                    create table if not exists user_container_action (
                                        action_id varchar(36),
                                        user_id varchar(36),
                                        timestamp timestamp not null default current_timestamp,
                                        primary key  (action_id, user_id),
                                        FOREIGN KEY (action_id) REFERENCES notify_container(id) ON DELETE CASCADE
                        
                                    )
                        """).then().doOnSuccess(_ -> log.info("Created new user_container_action table"))
                .doOnError(e -> log.error("Failed to create user_container_action table", e))
                .block();

        databaseClient.sql("""
                          create index if not exists idx_action_user
                            on user_container_action (user_id)
                        """).then().doOnSuccess(_ -> log.info("Created new index on user_container_action table"))
                .doOnError(e -> log.error("Failed to create index on user_container_action table", e))
                .block();
    }
}

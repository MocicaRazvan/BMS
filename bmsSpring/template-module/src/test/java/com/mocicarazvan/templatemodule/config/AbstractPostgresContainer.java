//package com.mocicarazvan.templatemodule.config;
//
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//@Testcontainers
//public abstract class AbstractPostgresContainer {
//
//
//    @Container
//    @ServiceConnection
//    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.3-alpine")
//            .withInitScript("schema-test.sql");
//
//
//    public abstract void seed();
//
//
//}

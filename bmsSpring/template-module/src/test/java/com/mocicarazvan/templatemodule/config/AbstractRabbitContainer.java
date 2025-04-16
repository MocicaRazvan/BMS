//package com.mocicarazvan.templatemodule.config;
//
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.RabbitMQContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//@Testcontainers
//public abstract class AbstractRabbitContainer {
//
//    @Container
//    @ServiceConnection
//    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.13.7-management-alpine")
//            .withExposedPorts(5672, 15672)
//            .withEnv("RABBITMQ_DEFAULT_USER", "guest")
//            .withEnv("RABBITMQ_DEFAULT_PASS", "guest");
//
/// /    @DynamicPropertySource
/// /    static void registerRabbitProperties(DynamicPropertyRegistry registry) {
/// /        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
/// /        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
/// /        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
/// /        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
/// /    }
//}

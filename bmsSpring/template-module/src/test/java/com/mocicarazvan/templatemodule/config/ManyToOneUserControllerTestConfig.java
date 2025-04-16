package com.mocicarazvan.templatemodule.config;


import com.mocicarazvan.templatemodule.controllers.beans.ManyToOneUserControllerBeanTest;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ManyToOneUserControllerTestConfig {
    @Bean
    public ManyToOneUserControllerBeanTest.ManyToOneLinkBuilder manyToOneLinkBuilder() {
        return new ManyToOneUserControllerBeanTest.ManyToOneLinkBuilder();
    }

    @Bean
    public RequestsUtils requestsUtils() {
        return new RequestsUtils();
    }
}

package com.mocicarazvan.templatemodule.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.ReactiveTransactionManager;

@TestConfiguration(proxyBeanMethods = false)
public class LocalTestConfig {
    @Bean
    public TrxStepVerifier trxStepVerifier(ReactiveTransactionManager transactionManager) {
        return new TrxStepVerifier(transactionManager);
    }

}

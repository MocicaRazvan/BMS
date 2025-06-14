package com.mocicarazvan.fileservice.config;


import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mocicarazvan.fileservice.objectMapper.BeanDeserializerModifierWithValidation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {
    @Bean
    public Module validationModule() {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifierWithValidation());
        return module;
    }
}

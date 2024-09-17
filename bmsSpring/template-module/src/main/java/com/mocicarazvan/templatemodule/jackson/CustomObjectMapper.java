package com.mocicarazvan.templatemodule.jackson;


import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mocicarazvan.templatemodule.jackson.validation.BeanDeserializerModifierWithValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CustomObjectMapper {

    private final Jackson2ObjectMapperBuilder builder;

    private final Module[] modules = new Module[]{new SimpleModule().addSerializer(WebFluxLinkBuilder.WebFluxLink.class, new WebFluxLinkSerializer())
            , new Jackson2HalModule(), new JavaTimeModule(), new SimpleModule().setDeserializerModifier(new BeanDeserializerModifierWithValidation())
    };

    public ObjectMapper customObjectMapper() {
        return builder
                .modules(modules)
                .build();
    }

    public ObjectMapper customObjectMapper(List<Module> additionalModules) {
        List<Module> combinedModules = new ArrayList<>(List.of(modules));
        combinedModules.addAll(additionalModules);

        return builder
                .modules(combinedModules.toArray(new Module[0]))
                .build();
    }
}

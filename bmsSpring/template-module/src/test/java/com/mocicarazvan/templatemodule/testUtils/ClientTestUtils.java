package com.mocicarazvan.templatemodule.testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.stream.Collectors;

public class ClientTestUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public static <T> String fromCollectionToNdjson(Collection<T> collection) {
        return collection.stream().map(item -> {
                    try {
                        return objectMapper.writeValueAsString(item);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.joining("\n"));
    }
}

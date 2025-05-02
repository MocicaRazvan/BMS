package com.mocicarazvan.dayservice.convertors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
@RequiredArgsConstructor
public class StringToJsonNodeConverter
        implements Converter<String, JsonNode> {

    private final ObjectMapper mapper;

    @Override
    public JsonNode convert(String source) {
        try {
            return mapper.readTree(source);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid JSON", e);
        }
    }
}
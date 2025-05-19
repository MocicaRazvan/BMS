package com.mocicarazvan.archiveservice.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.UUID;

@WritingConverter
public class StringToUuidConverter implements Converter<String, UUID> {
    @Override
    public UUID convert(String source) {
        return (source != null ? UUID.fromString(source) : null);
    }
}
package com.mocicarazvan.archiveservice.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.UUID;

@ReadingConverter
public class UuidToStringConverter implements Converter<UUID, String> {
    @Override
    public String convert(UUID source) {
        return (source != null ? source.toString() : null);
    }
}
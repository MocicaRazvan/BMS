package com.mocicarazvan.templatemodule.convertors;

import org.springframework.core.convert.converter.Converter;

public abstract class BaseReadingConverter<E extends Enum<E>> implements Converter<String, E> {
    private final Class<E> enumType;

    protected BaseReadingConverter(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public E convert(String source) {
        return source == null ? null : Enum.valueOf(enumType, source);
    }
}
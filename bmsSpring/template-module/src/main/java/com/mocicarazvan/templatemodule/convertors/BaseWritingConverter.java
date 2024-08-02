package com.mocicarazvan.templatemodule.convertors;

import org.springframework.core.convert.converter.Converter;

public abstract class BaseWritingConverter<E extends Enum<E>> implements Converter<E, String> {
    private final Class<E> enumType;

    protected BaseWritingConverter(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public String convert(E en) {
        return en == null ? null : en.name();
    }

}

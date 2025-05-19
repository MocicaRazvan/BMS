package com.mocicarazvan.archiveservice.converters;


import com.mocicarazvan.archiveservice.dtos.enums.ContainerAction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ContainerActionReadingConverter implements Converter<String, ContainerAction> {
    @Override
    public ContainerAction convert(String source) {
        return ContainerAction.valueOf(source);
    }
}

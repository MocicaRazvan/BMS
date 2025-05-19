package com.mocicarazvan.archiveservice.converters;


import com.mocicarazvan.archiveservice.dtos.enums.ContainerAction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ContainerActionWritingConverter implements Converter<ContainerAction, String> {

    @Override
    public String convert(ContainerAction source) {
        return source == null ? null : source.name();
    }
}

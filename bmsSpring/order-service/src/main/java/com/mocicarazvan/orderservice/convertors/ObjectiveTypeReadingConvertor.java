package com.mocicarazvan.orderservice.convertors;


import com.mocicarazvan.orderservice.enums.ObjectiveType;
import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ObjectiveTypeReadingConvertor extends BaseReadingConverter<ObjectiveType> {
    public ObjectiveTypeReadingConvertor() {
        super(ObjectiveType.class);
    }
}

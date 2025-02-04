package com.mocicarazvan.orderservice.convertors;


import com.mocicarazvan.orderservice.enums.ObjectiveType;
import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ObjectiveTypeWritingConvertor extends BaseWritingConverter<ObjectiveType> {
    public ObjectiveTypeWritingConvertor() {
        super(ObjectiveType.class);
    }
}

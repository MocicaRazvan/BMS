package com.mocicarazvan.planservice.convertors;


import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ObjectiveTypeWritingConvertor extends BaseWritingConverter<ObjectiveType> {
    public ObjectiveTypeWritingConvertor() {
        super(ObjectiveType.class);
    }
}

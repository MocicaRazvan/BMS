package com.mocicarazvan.planservice.convertors;


import com.mocicarazvan.planservice.enums.ObjectiveType;
import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ObjectiveTypeReadingConvertor extends BaseReadingConverter<ObjectiveType> {
    public ObjectiveTypeReadingConvertor() {
        super(ObjectiveType.class);
    }
}

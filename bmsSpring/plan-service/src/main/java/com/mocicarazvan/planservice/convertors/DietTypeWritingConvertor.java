package com.mocicarazvan.planservice.convertors;


import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class DietTypeWritingConvertor extends BaseWritingConverter<DietType> {

    public DietTypeWritingConvertor() {
        super(DietType.class);
    }
}

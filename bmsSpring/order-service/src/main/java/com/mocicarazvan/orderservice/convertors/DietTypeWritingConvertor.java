package com.mocicarazvan.orderservice.convertors;


import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class DietTypeWritingConvertor extends BaseWritingConverter<DietType> {

    public DietTypeWritingConvertor() {
        super(DietType.class);
    }
}

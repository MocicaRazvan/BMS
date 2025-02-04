package com.mocicarazvan.orderservice.convertors;


import com.mocicarazvan.orderservice.enums.DietType;
import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class DietTypeReadingConvertor extends BaseReadingConverter<DietType> {
    public DietTypeReadingConvertor() {
        super(DietType.class);
    }
}

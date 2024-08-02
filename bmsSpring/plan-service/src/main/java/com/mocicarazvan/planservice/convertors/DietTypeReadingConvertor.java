package com.mocicarazvan.planservice.convertors;


import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class DietTypeReadingConvertor extends BaseReadingConverter<DietType> {
    public DietTypeReadingConvertor() {
        super(DietType.class);
    }
}

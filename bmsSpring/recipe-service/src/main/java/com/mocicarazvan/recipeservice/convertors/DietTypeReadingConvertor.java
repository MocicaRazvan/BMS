package com.mocicarazvan.recipeservice.convertors;


import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class DietTypeReadingConvertor extends BaseReadingConverter<DietType> {
    public DietTypeReadingConvertor() {
        super(DietType.class);
    }
}

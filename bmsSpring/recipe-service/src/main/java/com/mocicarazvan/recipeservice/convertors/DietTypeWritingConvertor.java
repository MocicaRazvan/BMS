package com.mocicarazvan.recipeservice.convertors;


import com.mocicarazvan.recipeservice.enums.DietType;
import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class DietTypeWritingConvertor extends BaseWritingConverter<DietType> {

    public DietTypeWritingConvertor() {
        super(DietType.class);
    }
}

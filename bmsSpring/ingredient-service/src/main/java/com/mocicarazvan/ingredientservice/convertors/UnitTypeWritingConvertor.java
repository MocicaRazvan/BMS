package com.mocicarazvan.ingredientservice.convertors;


import com.mocicarazvan.ingredientservice.enums.UnitType;
import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class UnitTypeWritingConvertor extends BaseWritingConverter<UnitType> {
    public UnitTypeWritingConvertor() {
        super(UnitType.class);
    }
}

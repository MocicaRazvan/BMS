package com.mocicarazvan.ingredientservice.convertors;

import com.mocicarazvan.ingredientservice.enums.UnitType;
import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class UnitTypeReadingConvertor extends BaseReadingConverter<UnitType> {
    public UnitTypeReadingConvertor() {
        super(UnitType.class);
    }
}

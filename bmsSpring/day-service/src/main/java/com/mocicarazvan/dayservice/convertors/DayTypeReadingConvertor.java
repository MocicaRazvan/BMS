package com.mocicarazvan.dayservice.convertors;

import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class DayTypeReadingConvertor extends BaseReadingConverter<DayType> {
    public DayTypeReadingConvertor() {
        super(DayType.class);
    }
}

package com.mocicarazvan.dayservice.convertors;


import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class DayTypeWritingConvertor extends BaseWritingConverter<DayType> {
    public DayTypeWritingConvertor() {
        super(DayType.class);
    }
}

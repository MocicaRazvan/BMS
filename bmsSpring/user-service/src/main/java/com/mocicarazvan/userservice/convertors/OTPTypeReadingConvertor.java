package com.mocicarazvan.userservice.convertors;

import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import com.mocicarazvan.userservice.enums.OTPType;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class OTPTypeReadingConvertor extends BaseReadingConverter<OTPType> {
    public OTPTypeReadingConvertor() {
        super(OTPType.class);
    }
}
package com.mocicarazvan.userservice.convertors;


import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import com.mocicarazvan.userservice.enums.OTPType;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class OTPTypeWritingConverter extends BaseWritingConverter<OTPType> {
    public OTPTypeWritingConverter() {
        super(OTPType.class);
    }
}
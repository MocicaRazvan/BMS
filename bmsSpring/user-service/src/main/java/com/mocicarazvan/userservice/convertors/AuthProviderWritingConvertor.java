package com.mocicarazvan.userservice.convertors;


import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class AuthProviderWritingConvertor extends BaseWritingConverter<AuthProvider> {
    public AuthProviderWritingConvertor() {
        super(AuthProvider.class);
    }
}
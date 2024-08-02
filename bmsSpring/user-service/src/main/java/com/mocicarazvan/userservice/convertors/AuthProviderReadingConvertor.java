package com.mocicarazvan.userservice.convertors;


import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter


public class AuthProviderReadingConvertor extends BaseReadingConverter<AuthProvider> {
    public AuthProviderReadingConvertor() {
        super(AuthProvider.class);
    }
}
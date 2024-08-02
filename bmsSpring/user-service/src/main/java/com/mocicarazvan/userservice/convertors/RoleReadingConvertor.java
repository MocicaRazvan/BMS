package com.mocicarazvan.userservice.convertors;


import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import com.mocicarazvan.templatemodule.enums.Role;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter


public class RoleReadingConvertor extends BaseReadingConverter<Role> {
    public RoleReadingConvertor() {
        super(Role.class);
    }
}
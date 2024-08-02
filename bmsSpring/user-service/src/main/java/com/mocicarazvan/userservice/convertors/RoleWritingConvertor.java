package com.mocicarazvan.userservice.convertors;

import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import com.mocicarazvan.templatemodule.enums.Role;
import org.springframework.data.convert.WritingConverter;

@WritingConverter


public class RoleWritingConvertor extends BaseWritingConverter<Role> {
    public RoleWritingConvertor() {
        super(Role.class);
    }
}
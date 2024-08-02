package com.mocicarazvan.userservice.mappers;


import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.userservice.dtos.auth.requests.RegisterRequest;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.models.UserCustom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    @Autowired
    protected PasswordEncoder passwordEncoder;


    @Mapping(target = "password", expression = "java(passwordEncoder.encode(registerRequest.getPassword()))")
    @Mapping(target = "role", expression = "java(com.mocicarazvan.templatemodule.enums.Role.ROLE_USER)")
    public abstract UserCustom fromRegisterRequestToUserCustom(RegisterRequest registerRequest);

    public abstract AuthResponse fromUserCustomToAuthResponse(UserCustom userCustom);

    public abstract UserDto fromUserCustomToUserDto(UserCustom userCustom);
}

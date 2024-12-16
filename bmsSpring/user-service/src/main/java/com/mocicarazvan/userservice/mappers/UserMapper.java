package com.mocicarazvan.userservice.mappers;


import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.userservice.dtos.auth.requests.RegisterRequest;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.models.UserCustom;
import io.r2dbc.spi.Row;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    @Autowired
    protected PasswordEncoder passwordEncoder;


    @Mapping(target = "password", expression = "java(passwordEncoder.encode(registerRequest.getPassword()))")
    @Mapping(target = "role", expression = "java(com.mocicarazvan.templatemodule.enums.Role.ROLE_USER)")
    public abstract UserCustom fromRegisterRequestToUserCustom(RegisterRequest registerRequest);

    public abstract AuthResponse fromUserCustomToAuthResponse(UserCustom userCustom);

    public abstract UserDto fromUserCustomToUserDto(UserCustom userCustom);

    public UserCustom fromRowToModel(Row row) {
        return UserCustom.builder()
                .firstName(row.get("first_name", String.class))
                .lastName(row.get("last_name", String.class))
                .email(row.get("email", String.class))
                .role(Role.valueOf(Objects.requireNonNull(row.get("role", String.class)).toUpperCase()))
                .image(row.get("image", String.class))
                .provider(AuthProvider.valueOf(Objects.requireNonNull(row.get("provider", String.class)).toUpperCase()))
                .emailVerified(Boolean.TRUE.equals(row.get("is_email_verified", Boolean.class)))
                .id(row.get("id", Long.class))
                .createdAt(row.get("created_at", java.time.LocalDateTime.class))
                .updatedAt(row.get("updated_at", java.time.LocalDateTime.class))
                .build();
    }
}

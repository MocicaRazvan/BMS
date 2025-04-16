package com.mocicarazvan.templatemodule.dtos.response;

import com.mocicarazvan.templatemodule.dtos.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Data
@SuperBuilder
@AllArgsConstructor
public class ResponseWithUserDto<T> {
    private T model;
    private UserDto user;
}

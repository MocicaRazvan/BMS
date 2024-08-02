package com.mocicarazvan.templatemodule.dtos.response;


import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseWithUserDtoEntity<T> {
    private CustomEntityModel<T> model;
    private UserDto user;
}

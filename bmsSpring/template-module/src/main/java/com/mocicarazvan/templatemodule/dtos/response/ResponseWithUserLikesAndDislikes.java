package com.mocicarazvan.templatemodule.dtos.response;

import com.mocicarazvan.templatemodule.dtos.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Schema(description = "The comment response dto")
@AllArgsConstructor
public class ResponseWithUserLikesAndDislikes<T> extends ResponseWithUserDto<T> {
    private List<UserDto> userLikes;
    private List<UserDto> userDislikes;


}

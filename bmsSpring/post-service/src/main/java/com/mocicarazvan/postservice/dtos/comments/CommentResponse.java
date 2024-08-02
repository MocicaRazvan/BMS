package com.mocicarazvan.postservice.dtos.comments;


import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyUserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
@SuperBuilder
@Schema(description = "The comment response dto")
public class CommentResponse extends TitleBodyUserDto {

    @Schema(description = "The post's id for which the comment belongs")
    private Long referenceId;
}

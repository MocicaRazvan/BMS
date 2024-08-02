package com.mocicarazvan.commentservice.dtos;


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

    @Schema(description = "The reference's id for which the comment belongs")
    private Long referenceId;
}

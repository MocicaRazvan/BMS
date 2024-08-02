package com.mocicarazvan.commentservice.dtos;

import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
@SuperBuilder
public class CommentBody extends TitleBodyDto {
}

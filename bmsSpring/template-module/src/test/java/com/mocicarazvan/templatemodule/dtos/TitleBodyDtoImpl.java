package com.mocicarazvan.templatemodule.dtos;

import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@SuperBuilder
public class TitleBodyDtoImpl extends TitleBodyDto {
}

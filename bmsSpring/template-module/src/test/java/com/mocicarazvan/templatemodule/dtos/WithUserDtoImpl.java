package com.mocicarazvan.templatemodule.dtos;

import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
@SuperBuilder
public class WithUserDtoImpl extends WithUserDto {
}

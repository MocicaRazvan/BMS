package com.mocicarazvan.planservice.dtos.dayClient;

import com.mocicarazvan.planservice.enums.DayType;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class DayResponse extends TitleBodyUserDto {
    private DayType type;
}

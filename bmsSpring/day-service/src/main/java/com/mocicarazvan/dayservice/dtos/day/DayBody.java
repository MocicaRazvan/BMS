package com.mocicarazvan.dayservice.dtos.day;

import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
import jakarta.validation.constraints.NotNull;
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
public class DayBody extends TitleBodyDto {

    @NotNull(message = "The type should not be null.")
    private DayType type;
}

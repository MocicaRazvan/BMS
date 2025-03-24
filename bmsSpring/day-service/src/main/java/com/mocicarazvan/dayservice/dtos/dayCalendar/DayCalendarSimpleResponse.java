package com.mocicarazvan.dayservice.dtos.dayCalendar;

import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DayCalendarSimpleResponse extends WithUserDto {
    private Long dayId;
    private LocalDate date;
}

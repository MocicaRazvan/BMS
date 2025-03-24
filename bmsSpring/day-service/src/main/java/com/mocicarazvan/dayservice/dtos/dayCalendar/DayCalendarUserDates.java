package com.mocicarazvan.dayservice.dtos.dayCalendar;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayCalendarUserDates {
    private Long id;
    private LocalDate customDate;
}

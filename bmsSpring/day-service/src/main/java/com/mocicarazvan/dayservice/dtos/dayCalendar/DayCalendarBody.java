package com.mocicarazvan.dayservice.dtos.dayCalendar;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayCalendarBody {
    @NotNull(message = "The dayId should be present")
    private Long dayId;

    @NotNull(message = "The date should be present")
    private LocalDate date;
}

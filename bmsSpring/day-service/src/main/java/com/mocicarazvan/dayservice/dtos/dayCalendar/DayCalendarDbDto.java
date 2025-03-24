package com.mocicarazvan.dayservice.dtos.dayCalendar;

import com.mocicarazvan.dayservice.models.DayCalendar;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayCalendarDbDto extends DayCalendar {
    private String day;
    private String meals;
}

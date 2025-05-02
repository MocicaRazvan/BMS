package com.mocicarazvan.dayservice.dtos.dayCalendar;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayCalendarTrackingStats {
    private Long userId;
    private int year;
    private int month;
    private JsonNode typeCounts;
}

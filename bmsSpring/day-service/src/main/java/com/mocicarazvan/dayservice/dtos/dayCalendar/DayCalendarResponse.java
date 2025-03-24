package com.mocicarazvan.dayservice.dtos.dayCalendar;

import com.mocicarazvan.dayservice.dtos.day.DayResponse;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

public class DayCalendarResponse<T> extends WithUserDto {
    private DayResponse dayResponse;
    private List<T> mealResponses;
    private LocalDate date;
    private UserDto author;

    public <M> DayCalendarResponse<M> cloneWithMeals(List<M> meals) {
        return DayCalendarResponse.<M>builder()
                .userId(this.getUserId())
                .id(this.getId())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .dayResponse(this.dayResponse)
                .mealResponses(meals)
                .date(this.date)
                .author(this.author)
                .build();
    }

    public static <T> DayCalendarResponse<T> fromSimpleResponse(DayCalendarSimpleResponse simpleResponse) {
        return DayCalendarResponse.<T>builder()
                .date(simpleResponse.getDate())
                .userId(simpleResponse.getUserId())
                .id(simpleResponse.getId())
                .createdAt(simpleResponse.getCreatedAt())
                .updatedAt(simpleResponse.getUpdatedAt())
                .build();
    }

}

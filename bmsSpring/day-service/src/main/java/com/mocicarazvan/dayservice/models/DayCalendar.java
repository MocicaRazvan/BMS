package com.mocicarazvan.dayservice.models;

import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("day_calendar")
public class DayCalendar extends ManyToOneUser {
    private Long dayId;

    @Column("custom_date")
    private LocalDate date;

    @Override
    public DayCalendar clone() {
        return (DayCalendar) super.clone();
    }
}

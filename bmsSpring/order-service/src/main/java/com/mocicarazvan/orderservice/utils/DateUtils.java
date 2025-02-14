package com.mocicarazvan.orderservice.utils;

import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateUtils {

    public static Pair<LocalDateTime, LocalDateTime> getIntervalDates(LocalDate from, LocalDate to) {

        if (from.isAfter(to)) {
            LocalDate temp = from;
            from = to;
            to = temp;
        }

        LocalDateTime startDate = from.atStartOfDay();
        LocalDateTime endDate = to.atStartOfDay().plusDays(1);
        return Pair.of(startDate, endDate);
    }

    public static Pair<LocalDateTime, LocalDateTime> getMonthRange(LocalDate month) {
        return getIntervalDates(month.withDayOfMonth(1), month.withDayOfMonth(month.lengthOfMonth()));
    }

}

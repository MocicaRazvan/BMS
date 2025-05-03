package com.mocicarazvan.dayservice.repositories;

import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarDbDto;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarTrackingStats;
import com.mocicarazvan.dayservice.dtos.dayCalendar.DayCalendarUserDates;
import com.mocicarazvan.dayservice.models.DayCalendar;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DayCalendarRepository extends R2dbcRepository<DayCalendar, Long> {

    Flux<DayCalendar> findAllByUserIdAndDateBetween(Long userId, LocalDate dateAfter, LocalDate dateBefore);

    Mono<Boolean> existsByIdAndUserId(Long id, Long userId);

    @Query("""
                select dc.*,
                       row_to_json(d) as day,
                       (select json_agg(m) from meal m where m.day_id = dc.day_id) as meals
                from day_calendar dc
                join day d on d.id = dc.day_id
                where dc.id = :id
                and dc.user_id = :userId
            """)
    Mono<DayCalendarDbDto> findFullDayCalendarByIdAndUserId(Long id, Long userId);

    @Query("""
                select dc.*,
                       row_to_json(d) as day,
                       (select json_agg(m) from meal m where m.day_id = dc.day_id) as meals
                from day_calendar dc
                join day d on d.id = dc.day_id
                where dc.user_id = :userId
                and dc.custom_date >= :dateAfter
                and dc.custom_date <= :dateBefore
            """)
    Flux<DayCalendarDbDto> findAllFullDayCalendarsByUserIdAndDateBetween(Long userId, LocalDate dateAfter, LocalDate dateBefore);

    @Query("""
                select dc.custom_date,
                       dc.id
                from day_calendar dc
                where dc.user_id = :userId
            """)
    Flux<DayCalendarUserDates> findAllUserDatesByUserId(Long userId);


    @Query("""
                      WITH type_agg AS (
                        SELECT
                          MAX(dc.user_id) AS user_id,
                          EXTRACT(YEAR FROM dc.custom_date) AS year,
                          EXTRACT(MONTH FROM dc.custom_date) AS month,
                          d.type,
                          COUNT(*) AS cnt
                        FROM day_calendar dc
                        JOIN day d ON dc.day_id = d.id
                        WHERE ( :dateAfter IS NULL OR  dc.custom_date >= :dateAfter )
                          AND ( :dateBefore IS NULL OR  dc.custom_date <= :dateBefore )
                          AND dc.user_id = :userId
                        GROUP BY year, month, d.type
                      )
                      SELECT
                        MAX(user_id) AS user_id,
                        year,
                        month,
                        JSONB_OBJECT_AGG(type, cnt)::text AS type_counts
                      FROM type_agg
                      GROUP BY year, month
                      ORDER BY year, month
            """)
    Flux<DayCalendarTrackingStats> findDayCalendarTrackingStats(
            Long userId,
            LocalDateTime dateAfter,
            LocalDateTime dateBefore
    );

    Mono<Boolean> existsByIdAndUserIdAndDayId(Long id, Long userId, Long dayId);

}

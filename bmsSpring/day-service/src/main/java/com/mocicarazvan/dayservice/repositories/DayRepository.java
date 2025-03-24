package com.mocicarazvan.dayservice.repositories;


import com.mocicarazvan.dayservice.dtos.day.DayWithMealsDb;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.CountInParent;
import com.mocicarazvan.templatemodule.repositories.TitleBodyRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DayRepository extends TitleBodyRepository<Day>, CountInParent, CountIds {


    @Query("""
                select  distinct d.id from day d
                join meal m on m.day_id = d.id
                where :childId = any (m.recipes)
            """)
    Flux<Long> countInParent(Long childId);

    @Query("""
                select distinct d.id from day d
                where d.id in (:ids)
            """)
    Flux<Long> countByIds(List<Long> ids);

    @Query("""
            SELECT * FROM day
            WHERE EXTRACT(MONTH FROM created_at) = :month
            AND EXTRACT(YEAR FROM created_at) = :year
            ORDER BY created_at DESC
            """)
    Flux<Day> findModelByMonth(int month, int year);

    @Query("""
            select d.*,(select json_agg(m) from meal m where m.day_id = d.id) as meals
            from day d
            where d.id = :id
            """)
    Mono<DayWithMealsDb> findDayWithMeals(Long id);

    @Query("""
            select d.*,(select json_agg(m) from meal m where m.day_id = d.id) as meals
            from day d
            where d.id in (:ids)
            """)
    Flux<DayWithMealsDb> findDaysWithMeals(List<Long> ids);

}

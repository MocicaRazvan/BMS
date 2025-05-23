package com.mocicarazvan.dayservice.repositories;


import com.mocicarazvan.dayservice.dtos.day.DayWithMealsDb;
import com.mocicarazvan.dayservice.models.Day;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.CountInParent;
import com.mocicarazvan.templatemodule.repositories.TitleBodyRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public interface DayRepository extends TitleBodyRepository<Day>, CountInParent, CountIds {


    @Override
    @Query("""
                select coalesce(sum(mr.multiplicity),0) from day d
                join meal m on m.day_id = d.id
                join meal_recipes mr on mr.master_id=m.id
                where mr.child_id=:childId
            """)
    Mono<Long> countInParent(Long childId);

    @Override
    @Query("""
                select count(d.id) from day d
                where d.id in (:ids)
            """)
    Mono<Long> countByIds(Collection<Long> ids);

    @Query("""
            SELECT * FROM day
            WHERE created_at >= make_timestamp(:year, :month, 1, 0, 0, 0)
            AND created_at < make_timestamp(:year, :month, 1, 0, 0, 0) + INTERVAL '1 month'
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

package com.mocicarazvan.dayservice.repositories;


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
                select  count(*) from day d
                join meal m on m.day_id = d.id
                where :childId = any (m.recipes)
            """)
    Mono<Long> countInParent(Long childId);

    @Query("""
                select count(*) from day d
                where d.id in (:ids)
            """)
    Mono<Long> countByIds(List<Long> ids);

    @Query("""
            SELECT * FROM day
            WHERE EXTRACT(MONTH FROM created_at) = :month
            AND EXTRACT(YEAR FROM created_at) = :year
            ORDER BY created_at DESC
            """)
    Flux<Day> findModelByMonth(int month, int year);


}

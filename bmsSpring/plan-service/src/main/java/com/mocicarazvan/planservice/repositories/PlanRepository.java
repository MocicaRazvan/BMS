package com.mocicarazvan.planservice.repositories;

import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.CountInParent;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PlanRepository extends ApprovedRepository<Plan>, CountInParent, CountIds {

    @Query("""
                    select count(*) from plan p
                    where :childId = any (p.recipes)
            """)
    Mono<Long> countInParent(Long childId);


    @Query("""
                select count(*) from plan p
                where p.id in (:ids) and p.approved = true
            """)
    Mono<Long> countByIds(List<Long> ids);

    Flux<Plan> findAllByIdInAndApprovedTrue(List<Long> ids);

    @Query("""
            SELECT * FROM comment
            WHERE EXTRACT(MONTH FROM created_at) = :month
            AND EXTRACT(YEAR FROM created_at) = :year
            ORDER BY created_at DESC
            """)
    Flux<Plan> findModelByMonth(int month, int year);

    Flux<Plan> findAllByUserId(Long userId);
}

package com.mocicarazvan.planservice.repositories;

import com.mocicarazvan.planservice.dtos.PlanWithSimilarity;
import com.mocicarazvan.planservice.models.Plan;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import com.mocicarazvan.templatemodule.repositories.CountIds;
import com.mocicarazvan.templatemodule.repositories.CountInParent;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public interface PlanRepository extends ApprovedRepository<Plan>, CountInParent, CountIds {

    @Query("""
                    select  coalesce(sum(pd.multiplicity),0) from plan p
                    join plan_days pd on p.id=pd.master_id
                    where pd.child_id= :childId
            """)
    Mono<Long> countInParent(Long childId);

    @Override
    @Query("""
                select count(p.id)  from plan p
                where p.approved = true and p.id in (:ids)
            """)
    Mono<Long> countByIds(Collection<Long> ids);

    Flux<Plan> findAllByIdInAndApprovedTrue(List<Long> ids);

    @Query("""
            SELECT * FROM plan
            WHERE created_at >= make_timestamp(:year, :month, 1, 0, 0, 0)
            AND created_at < make_timestamp(:year, :month, 1, 0, 0, 0) + INTERVAL '1 month'
            ORDER BY created_at DESC
            """)
    Flux<Plan> findModelByMonth(int month, int year);

    Flux<Plan> findAllByUserId(Long userId);

    @Query("""
            with emb as(
                select p.*,
                       array [p.objective, p.type] as arr,
                       pe.embedding
                from plan p
                         join plan_embedding pe on p.id = pe.entity_id)
            select
                (2*sub.ip+2*sub.ti+sub.ps)/5 as similarity,
                sub.*
            from (
                     select
                         -(e1.embedding <#> e2.embedding) as ip,
                         coalesce(
                                 (
                                     SELECT count(x.arr)
                                     FROM unnest(e1.arr) x(arr)
                                              JOIN unnest(e2.arr) y(arr) ON x.arr = y.arr
                                 )::float, 0
                         ) /cardinality(e1.arr) as ti, -- gandeste mai bine decat
                        COALESCE(1 - (ABS(e1.price - e2.price) / NULLIF(GREATEST(e1.price, e2.price), 0)), 0) AS ps,
                         e2.*
                     from
                         emb e1, emb e2
                     where e1.id=:id
                       and (
                           (e2.approved=true and e2.display=true
                               and e2.id !=ALL(:excludeIds)
                               )
                                or e2.id=e1.id)
                 ) as sub
            where (2*sub.ip+2*sub.ti+sub.ps)/5 >= :minSimilarity
            order by  similarity desc
            limit :limit
            
            """)
    Flux<PlanWithSimilarity> getSimilarPlans(Long id,
                                             Long[] excludeIds,
                                             int limit,
                                             Double minSimilarity
    );
}

package com.mocicarazvan.orderservice.repositories;

import com.mocicarazvan.orderservice.models.Order;
import com.mocicarazvan.templatemodule.dtos.IdsDto;
import com.mocicarazvan.templatemodule.repositories.CountInParent;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ManyToOneUserRepository<Order>, CountInParent {


    @Query("""
            SELECT count(*)
            FROM custom_order o
            join plan_order po
            on o.id = po.order_id
            WHERE o.user_id = :userId
              AND po.plan_id in (:planIds)
            """)
    Mono<Boolean> existsUserWithPlan(Long userId, Long[] planIds);


    @Query("""
            select distinct unnest(plan_ids) from custom_order
            where user_id=:userId
            """)
    Flux<Long> findUserPlanIds(Long userId);


    @Override
    @Query("""
              SELECT count(o.id)
                FROM custom_order o
                JOIN plan_order   p
                  ON p.order_id = o.id
                 AND p.plan_id  = :childId
            """)
    Mono<Long> countInParent(Long childId);

    @Query("""
              SELECT EXISTS (
                SELECT 1
                  FROM custom_order o
                  JOIN plan_order   p
                    ON p.order_id = o.id
                   AND p.plan_id  = :planId
                 WHERE o.user_id = :userId
              )
            """)
    Mono<Boolean> existsByUserIdAndPlanId(Long userId, Long planId);

    @Query("""
            SELECT * FROM custom_order
            WHERE created_at >= make_timestamp(:year, :month, 1, 0, 0, 0)
            AND created_at < make_timestamp(:year, :month, 1, 0, 0, 0) + INTERVAL '1 month'
            ORDER BY created_at DESC
            """)
    Flux<Order> findModelByMonth(int month, int year);


    @Query(
            """
                    select o.id, o.plan_ids as reference_ids
                    from custom_order o
                    where o.user_id=:userId
                    """
    )
    Flux<IdsDto> findPlansByUserId(Long userId);


}

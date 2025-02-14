package com.mocicarazvan.orderservice.repositories;

import com.mocicarazvan.orderservice.models.Order;
import com.mocicarazvan.templatemodule.repositories.CountInParent;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ManyToOneUserRepository<Order>, CountInParent {


    @Query("""
            SELECT count(*) > 0
                FROM custom_order o
                WHERE o.user_id = :userId
                AND o.plan_ids && :planIds
            """)
    Mono<Boolean> existsUserWithPlan(Long userId, Long[] planIds);


    @Query("""
            select distinct unnest(plan_ids) from custom_order
            where user_id=:userId
            """)
    Flux<Long> findUserPlanIds(Long userId);


    @Override
    @Query("""
                select distinct o.id from custom_order o
                where :childId = any (o.plan_ids)
            """)
    Flux<Long> countInParent(Long childId);

    @Query("SELECT COUNT(*) > 0 FROM custom_order WHERE user_id = :userId AND :planId = ANY(plan_ids)")
    Mono<Boolean> existsByUserIdAndPlanId(Long userId, Long planId);

    @Query("""
            SELECT * FROM custom_order
            WHERE EXTRACT(MONTH FROM created_at) = :month
            AND EXTRACT(YEAR FROM created_at) = :year
            ORDER BY created_at DESC
            """)
    Flux<Order> findModelByMonth(int month, int year);


}

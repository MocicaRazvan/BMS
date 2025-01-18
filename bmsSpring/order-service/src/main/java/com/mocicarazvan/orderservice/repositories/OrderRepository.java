package com.mocicarazvan.orderservice.repositories;

import com.mocicarazvan.orderservice.dtos.summaries.CountryOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.DailyOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.MonthlyOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.TopUsersSummary;
import com.mocicarazvan.orderservice.dtos.summaries.trainer.DailyTrainerOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.trainer.MonthlyTrainerOrderSummary;
import com.mocicarazvan.orderservice.models.Order;
import com.mocicarazvan.templatemodule.repositories.CountInParent;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

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

//    @Query("""
//                   SELECT o.id AS orderId, o.user_id AS userId, o.plan_ids AS planIds, o.total AS total,
//                   o.created_at AS orderCreatedAt, o.updated_at AS orderUpdatedAt,
//                   a.id AS addressId, a.city AS city, a.country AS country, a.line1 AS line1,
//                   a.line2 AS line2, a.postal_code AS postalCode, a.state AS state,
//                   a.created_at AS addressCreatedAt, a.updated_at AS addressUpdatedAt
//            FROM custom_order o
//            JOIN address a ON o.address_id = a.id
//            WHERE o.id = :id
//             """)
//    Mono<OrderWithAddress> findByIdWithAddress(Long id);

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

    @Query("""
            SELECT EXTRACT(YEAR FROM created_at) AS year, 
                   EXTRACT(MONTH FROM created_at) AS month, 
                   COUNT(*) AS count, 
                   SUM(total) AS total_amount
            FROM custom_order
            WHERE created_at >= :startDate AND created_at < :endDate
            GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at)
            ORDER BY year, month
            """)
    Flux<MonthlyOrderSummary> getOrdersSummaryByDateRangeGroupedByMonth(
            LocalDateTime startDate,
            LocalDateTime endDate);

    @Query("""
            SELECT EXTRACT(YEAR FROM co.created_at) AS year,
                       EXTRACT(MONTH FROM co.created_at) AS month,
                       COUNT(*) AS count,
                       array_agg(u.plan_id) AS plan_ids
            FROM
                custom_order co,
                LATERAL unnest(co.plan_ids) AS u(plan_id)
            WHERE created_at >= :startDate AND created_at < :endDate
            GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at)
            ORDER BY year, month
            """)
    Flux<MonthlyTrainerOrderSummary> getTrainerOrdersSummaryByDateRangeGroupedByMonth(
            LocalDateTime startDate,
            LocalDateTime endDate);

    @Query("""
            SELECT EXTRACT(YEAR FROM created_at) AS year, 
                   EXTRACT(MONTH FROM created_at) AS month, 
                     EXTRACT(DAY FROM created_at) AS day,
                   COUNT(*) AS count, 
                   SUM(total) AS total_amount
            FROM custom_order
            WHERE created_at >= :startDate AND created_at < :endDate
            GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at), EXTRACT(DAY FROM created_at)
            ORDER BY year, month, day
            """)
    Flux<DailyOrderSummary> getOrdersSummaryByDateRangeGroupedByDay(
            LocalDateTime startDate,
            LocalDateTime endDate);

    @Query("""
            SELECT EXTRACT(YEAR FROM created_at) AS year, 
                   EXTRACT(MONTH FROM created_at) AS month, 
                    EXTRACT(DAY FROM created_at) AS day,
                   COUNT(*) AS count,
                     array_agg(u.plan_id) AS plan_ids
            FROM
                custom_order co,
                LATERAL unnest(co.plan_ids) AS u(plan_id)
            WHERE created_at >= :startDate AND created_at < :endDate
            GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at), EXTRACT(DAY FROM created_at)
            ORDER BY year, month, day
            """)
    Flux<DailyTrainerOrderSummary> getTrainerOrdersSummaryByDateRangeGroupedByDay(
            LocalDateTime startDate,
            LocalDateTime endDate);

    @Query("""
                select sub.*,
                    MAX(sub.total_amount) OVER () AS max_group_total,
                    MIN(sub.total_amount) OVER () AS min_group_total,
                    AVG(sub.total_amount) OVER () AS avg_group_total
                       from
                (SELECT
                    user_id,
                    sum(total) as total_amount,
                    count(id) as orders_number,
                    SUM(array_length(plan_ids, 1)) as plans_number,
                    ARRAY_AGG(DISTINCT elem) AS plan_values,
                    dense_rank() over( order by sum(total) desc) as rank
                FROM custom_order,
                     unnest(plan_ids) as elem
                WHERE created_at >= :startDate AND created_at < :endDate
                GROUP BY user_id) as sub
                where sub.rank <= :top
            """)
    Flux<TopUsersSummary> getTopUsersSummary(LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             int top);

    @Query("""
                SELECT
                   COUNT(*) AS value,
                    a.country as id
            FROM custom_order o
            JOIN address a on a.id = o.address_id
            GROUP BY a.country
            """)
    Flux<CountryOrderSummary> getOrdersCountByCountry();

    @Query("""
                SELECT
                     SUM(o.total) AS value,
                    a.country as id
            FROM custom_order o
            JOIN address a on a.id = o.address_id
            GROUP BY a.country
            """)
    Flux<CountryOrderSummary> getOrdersTotalByCountry();


}

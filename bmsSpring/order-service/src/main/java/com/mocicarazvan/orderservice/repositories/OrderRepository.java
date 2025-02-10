package com.mocicarazvan.orderservice.repositories;

import com.mocicarazvan.orderservice.dtos.summaries.*;
import com.mocicarazvan.orderservice.models.Order;
import com.mocicarazvan.templatemodule.repositories.CountInParent;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
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
                       COUNT(po.plan_id) AS count,
                        sum(po.price) as total_amount
            FROM
                custom_order co join plan_order po on co.id = po.order_id
            WHERE co.created_at >= :startDate AND co.created_at < :endDate
            and po.user_id = :trainerId
            GROUP BY EXTRACT(YEAR FROM co.created_at), EXTRACT(MONTH FROM co.created_at)
            ORDER BY year, month
            """)
    Flux<MonthlyOrderSummary> getTrainerOrdersSummaryByDateRangeGroupedByMonth(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long trainerId
    );

    @Query("""
                        with agg as (
                select co.created_at,
                       co.updated_at,
                       po.objective,
                       po.plan_id,
                       po.user_id,
                       po.price
                from custom_order co
                         join plan_order po on co.id = po.order_id
            
            )
            SELECT EXTRACT(YEAR FROM agg.created_at) AS year,
                   EXTRACT(MONTH FROM agg.created_at) AS month,
                   count(agg.plan_id) as count,
                   sum(agg.price) as total_amount,
                   agg.objective,
                   avg(agg.price) as average_amount
            
            FROM
                agg
              WHERE agg.created_at >= :startDate AND agg.created_at < :endDate
                         and (:trainerId =-1 or agg.user_id = :trainerId)
            GROUP BY EXTRACT(YEAR FROM agg.created_at), EXTRACT(MONTH FROM agg.created_at), objective
            ORDER BY year, month,objective
            
            """)
    Flux<MonthlyOrderSummaryObjective> getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectives(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long trainerId
    );

    @Query("""
            
             with agg as (
                select co.created_at,
                       co.updated_at,
                       po.plan_id,
                       po.user_id,
                       po.type,
                       po.price
                from custom_order co
                         join plan_order po on co.id = po.order_id
            
            )
            SELECT EXTRACT(YEAR FROM agg.created_at) AS year,
                   EXTRACT(MONTH FROM agg.created_at) AS month,
                   count(agg.plan_id) as count,
                   sum(agg.price) as total_amount,
                   agg.type,
                   avg(agg.price) as average_amount
            FROM
                agg
              WHERE agg.created_at >= :startDate AND agg.created_at < :endDate
                         and (:trainerId =-1 or agg.user_id = :trainerId)
            GROUP BY EXTRACT(YEAR FROM agg.created_at), EXTRACT(MONTH FROM agg.created_at), type
            ORDER BY year, month,type
            """)
    Flux<MonthlyOrderSummaryType> getTrainerOrdersSummaryByDateRangeGroupedByMonthTypes(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long trainerId
    );


    @Query("""
                    with agg as (
                        select co.created_at,
                               co.updated_at,
                               po.objective,
                               po.plan_id,
                               po.user_id,
                               po.type,
                               po.price
                        from custom_order co
                                 join plan_order po on co.id = po.order_id
            
                    )
                    SELECT EXTRACT(YEAR FROM agg.created_at) AS year,
                           EXTRACT(MONTH FROM agg.created_at) AS month,
                           count(agg.plan_id) as count,
                           sum(agg.price) as total_amount,
                           agg.objective,
                           agg.type,
                           avg(agg.price) as average_amount
            
                    FROM
                        agg
                     WHERE agg.created_at >= :startDate AND agg.created_at < :endDate
                                     and (:trainerId =-1 or agg.user_id = :trainerId)
                    GROUP BY EXTRACT(YEAR FROM agg.created_at), EXTRACT(MONTH FROM agg.created_at), objective,type
                    ORDER BY year, month,objective,type
            """)
    Flux<MonthlyOrderSummaryObjectiveType> getTrainerOrdersSummaryByDateRangeGroupedByMonthObjectivesTypes(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long trainerId
    );

    @Query("""
            SELECT EXTRACT(YEAR FROM co.created_at) AS year,
                   EXTRACT(MONTH FROM co.created_at) AS month,
                   sum(cardinality(co.plan_ids)) as count,
                   sum(co.total) as total_amount
            FROM
                custom_order co
            WHERE created_at >= :startDate AND created_at < :endDate
            GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at)
            ORDER BY year, month
            """)
    Flux<MonthlyOrderSummary> getAdminOrdersPlanSummaryByDateRangeGroupedByMonth(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

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
            SELECT EXTRACT(YEAR FROM co.created_at) AS year, 
                   EXTRACT(MONTH FROM co.created_at) AS month, 
                    EXTRACT(DAY FROM co.created_at) AS day,
                   COUNT(po.plan_id) AS count,
                   sum(po.price) as total_amount
            FROM
                custom_order co join plan_order po on co.id = po.order_id
            WHERE co.created_at >= :startDate AND co.created_at < :endDate
            and po.user_id = :trainerId
            GROUP BY EXTRACT(YEAR FROM co.created_at), EXTRACT(MONTH FROM co.created_at), EXTRACT(DAY FROM co.created_at)
            ORDER BY year, month, day
            """)
    Flux<DailyOrderSummary> getTrainerOrdersSummaryByDateRangeGroupedByDay(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long trainerId
    );

    @Query("""
            SELECT EXTRACT(YEAR FROM co.created_at) AS year,
                   EXTRACT(MONTH FROM co.created_at) AS month,
                   EXTRACT(DAY FROM created_at) AS day,
                   sum(cardinality(co.plan_ids)) as count,
                   sum(co.total) as total_amount
            FROM
                custom_order co
            WHERE created_at >= :startDate AND created_at < :endDate
            GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at), EXTRACT(DAY FROM created_at)
            ORDER BY year, month, day
            """)
    Flux<DailyOrderSummary> getAdminOrdersPlanSummaryByDateRangeGroupedByDay(
            LocalDateTime startDate,
            LocalDateTime endDate
    );


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
                WITH elem_counts AS (
                    SELECT elem, COUNT(*) AS cnt,
                           count(c.id) as cnt_ord
                    FROM custom_order c,
                         unnest(plan_ids) AS elem
                    WHERE created_at >= :startDate AND created_at < :endDate
                    GROUP BY elem
                )
                select * from (
                SELECT elem as plan_id,
                       cnt as count,
                       1.0 * cnt / NULLIF(SUM(cnt) OVER(),0) AS ratio,
                       MAX(cnt) OVER () AS max_group_count,
                       AVG(cnt) OVER () AS avg_group_count,
                       MIN(cnt) OVER () AS min_group_count,
                       DENSE_RANK() OVER (ORDER BY cnt DESC) AS rank
                FROM elem_counts) s
                where rank <= :top;
            """)
    Flux<TopPlansSummary> getTopPlansSummary(LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             int top);

    @Query("""
                WITH elem_counts AS (
                    SELECT elem, COUNT(*) AS cnt,
                           count(c.id) as cnt_ord
                    FROM custom_order c,
                         unnest(plan_ids) AS elem
                    WHERE created_at >= :startDate AND created_at < :endDate
                    and elem = any(:trainerPlanIds)
                    GROUP BY elem
                )
                select * from (
                SELECT elem as plan_id,
                       cnt as count,
                       1.0 * cnt / NULLIF(SUM(cnt) OVER(),0) AS ratio,
                       MAX(cnt) OVER () AS max_group_count,
                       AVG(cnt) OVER () AS avg_group_count,
                       MIN(cnt) OVER () AS min_group_count,
                       DENSE_RANK() OVER (ORDER BY cnt DESC) AS rank
                FROM elem_counts) s
                where rank <= :top;
            """)
    Flux<TopPlansSummary> getTopPlansSummaryTrainer(LocalDateTime startDate,
                                                    LocalDateTime endDate,
                                                    int top,
                                                    Long[] trainerPlanIds);

    @Query("""
                SELECT
                   COUNT(*) AS value,
                    a.country as id,
                    MAX(count(*)) OVER () AS max_group_total
            FROM custom_order o
            JOIN address a on a.id = o.address_id
            where (:from is null or o.created_at >= :from)
            and (:to is null or o.created_at <= :to)
            GROUP BY a.country
            """)
    Flux<CountryOrderSummary> getOrdersCountByCountry(LocalDate from, LocalDate to);

    @Query("""
                SELECT
                     SUM(o.total) AS value,
                     a.country as id,
                     MAX(SUM(o.total)) OVER () AS max_group_total
            FROM custom_order o
            JOIN address a on a.id = o.address_id
            where (:from is null or o.created_at >= :from)
            and (:to is null or o.created_at <= :to)
            GROUP BY a.country
            """)
    Flux<CountryOrderSummary> getOrdersTotalByCountry(LocalDate from, LocalDate to);


}

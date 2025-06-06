package com.mocicarazvan.orderservice.repositories;

import com.mocicarazvan.orderservice.dtos.summaries.*;
import com.mocicarazvan.orderservice.models.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SummaryRepository extends Repository<Order, Long> {
    @Query("""
            SELECT * FROM custom_order
            WHERE created_at >= make_timestamp(:year, :month, 1, 0, 0, 0)
            AND created_at < make_timestamp(:year, :month, 1, 0, 0, 0) + INTERVAL '1 month'
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
                order by rank
            """)
    Flux<TopUsersSummary> getTopUsersSummary(LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             int top);


    @Query("""
                WITH agg AS (
                    SELECT co.created_at,
                           co.updated_at,
                           po.objective,
                           po.plan_id,
                           po.user_id,
                           po.type,
                           po.price
                    FROM custom_order co
                    JOIN plan_order po ON co.id = po.order_id
                ),
                type_counts AS (
                    SELECT agg.user_id,
                           agg.type,
                           COUNT(*) AS type_count,
                           SUM(agg.price) AS type_amount,
                           AVG(agg.price) AS type_avg
                    FROM agg
                    WHERE agg.created_at >= :startDate
                      AND agg.created_at < :endDate
                    GROUP BY agg.user_id, agg.type
                ),
                objective_counts AS (
                    SELECT agg.user_id,
                           agg.objective,
                           COUNT(*) AS objective_count,
                           SUM(agg.price) AS objective_amount,
                           AVG(agg.price) AS objective_avg
                    FROM agg
                    WHERE agg.created_at >= :startDate
                      AND agg.created_at < :endDate
                    GROUP BY agg.user_id, agg.objective
                ),
                total_aggregated AS (
                    SELECT agg.user_id,
                           SUM(agg.price) AS total_amount,
                           COUNT( agg.plan_id) AS plan_count,
                           AVG(agg.price) AS average_amount
                    FROM agg
                    WHERE agg.created_at >= :startDate
                      AND agg.created_at < :endDate
                    GROUP BY agg.user_id
                ),
                aggregated AS (
                    SELECT ta.user_id,
                           ta.total_amount,
                           ta.plan_count,
                           ta.average_amount,
                           DENSE_RANK() OVER (ORDER BY ta.total_amount DESC) AS rank,
                           jsonb_object_agg(tc.type, tc.type_count) AS type_counts,
                           jsonb_object_agg(oc.objective, oc.objective_count) AS objective_counts,
                           jsonb_object_agg(oc.objective, oc.objective_amount) AS objective_amounts,
                           jsonb_object_agg(tc.type, tc.type_amount) AS type_amounts,
                           jsonb_object_agg(oc.objective, oc.objective_avg) AS objective_avgs,
                           jsonb_object_agg(tc.type, tc.type_avg) AS type_avgs
                    FROM total_aggregated ta
                    LEFT JOIN type_counts tc ON ta.user_id = tc.user_id
                    LEFT JOIN objective_counts oc ON ta.user_id = oc.user_id
                    GROUP BY ta.user_id, ta.total_amount, ta.plan_count, ta.average_amount
                ),
                filtered AS (
                        SELECT * FROM aggregated WHERE rank <= :top
                )
                SELECT f.*,
                       MAX(f.total_amount) OVER () AS max_group_total,
                       MIN(f.total_amount) OVER () AS min_group_total,
                       AVG(f.total_amount) OVER () AS avg_group_total,
                       MAX(f.plan_count) OVER () AS max_group_plan_count,
                       MIN(f.plan_count) OVER () AS min_group_plan_count,
                       AVG(f.plan_count) OVER () AS avg_group_plan_count
                FROM filtered f
                ORDER BY f.rank
            """)
    Flux<TopTrainersSummaryR2dbc> getTopTrainersSummary(LocalDateTime startDate,
                                                        LocalDateTime endDate,
                                                        int top);


    @Query("""
                WITH elem_counts AS (
                    SELECT elem, COUNT(*) AS cnt
                    FROM custom_order c,
                         unnest(plan_ids) AS elem
                    WHERE created_at >= :startDate AND created_at < :endDate
                    GROUP BY elem
                ),
                ranked_counts AS (
                    SELECT elem AS plan_id,
                           cnt AS count,
                           DENSE_RANK() OVER (ORDER BY cnt DESC) AS rank
                    FROM elem_counts
                ),
                top_counts AS (
                    SELECT * FROM ranked_counts
                    WHERE rank <= :top
                ),
                 final_counts AS (
                     SELECT *,
                            SUM(count) OVER () AS total_top_cnt
                     FROM top_counts
                 )
                SELECT *,
                       1.0 * count / NULLIF(total_top_cnt, 0) AS ratio,
                       MAX(count) OVER () AS max_group_count,
                       AVG(count) OVER () AS avg_group_count,
                       MIN(count) OVER () AS min_group_count
                FROM final_counts
            """)
    Flux<TopPlansSummary> getTopPlansSummary(LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             int top);

    @Query("""
                WITH elem_counts AS (
                    SELECT elem, COUNT(*) AS cnt
                    FROM custom_order c,
                         unnest(plan_ids) AS elem
                    WHERE created_at >= :startDate AND created_at < :endDate
                      AND elem = ANY(:trainerPlanIds)
                    GROUP BY elem
                ),
                ranked_counts AS (
                    SELECT elem AS plan_id,
                           cnt AS count,
                           1.0 * cnt / NULLIF(SUM(cnt) OVER (), 0) AS ratio,
                           DENSE_RANK() OVER (ORDER BY cnt DESC) AS rank
                    FROM elem_counts
                ),
                top_counts AS (
                    SELECT * FROM ranked_counts
                    WHERE rank <= :top
                )
                SELECT *,
                       MAX(count) OVER () AS max_group_count,
                       AVG(count) OVER () AS avg_group_count,
                       MIN(count) OVER () AS min_group_count
                FROM top_counts
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

    @Query("""
               select
                (select count(*) from custom_order) as orders_count,
                (select count(*) from plan_order) as plans_count
            """)
    Mono<OverallSummary> getOverallSummary();

}

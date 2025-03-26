package com.mocicarazvan.postservice.repositories;

import com.mocicarazvan.postservice.dtos.summaries.PostCountSummary;
import com.mocicarazvan.postservice.models.PostViewCount;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface PostViewCountRepository extends Repository<PostViewCount, Long> {

    @Modifying
    @Query("""
                    insert into post_view_count (post_id, view_count,access_date) values (:postId, :count, :accessedDate)
                    on conflict (post_id,access_date)
                    do update  set view_count = post_view_count.view_count + :count
            """)
    Mono<Void> incrementViewCount(Long postId, Long count, LocalDate accessedDate);

    @Query("""
                        select sum(view_count) from post_view_count where post_id = :postId
                        and (:accessedDateStart is null or access_date >= :accessedDateStart)
                        and (:accessedDateEnd is null or access_date <= :accessedDateEnd)
                        group by post_id
            """)
    Mono<Long> findCountByPostId(Long postId, LocalDate accessedDateStart, LocalDate accessedDateEnd);

    @Query("""
               with pvc_gr as(
                   select sum(pvc2.view_count) as view_count,
                          pvc2.post_id
                   from  post_view_count pvc2
                   where (:accessedDateStart is null or pvc2.access_date >= :accessedDateStart)
                     and (:accessedDateEnd is null or pvc2.access_date < :accessedDateEnd)
                   group by pvc2.post_id
               )
               select sub.* from
                   (select p.*, pvc.view_count,
                           dense_rank() over (order by pvc.view_count desc) as rank
                    from post p join pvc_gr pvc on p.id = pvc.post_id
                    where (:userId is null or p.user_id = :userId)
                   ) as sub
               where rank <= :top
               order by rank
            """)
    Flux<PostCountSummary> findTopPostsByCount(
            int top,
            Long userId,
            LocalDate accessedDateStart,
            LocalDate accessedDateEnd
    );
}

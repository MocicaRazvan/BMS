package com.mocicarazvan.postservice.repositories;


import com.mocicarazvan.postservice.dtos.PostWithSimilarity;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostRepository extends ApprovedRepository<Post> {
    Mono<Boolean> existsByIdAndApprovedIsTrue(Long id);

    @Query("""
            SELECT p.* FROM post p
            WHERE UPPER(p.title) LIKE UPPER(:title)
            AND p.approved = :approved 
            AND p.tags && cast(:tags as text[])
            """)
    Flux<Post> findAllByTitleContainingIgnoreCaseAndApprovedAndTagsIn(String title, boolean approved, String tags, PageRequest pageRequest);

    @Query("""
            SELECT COUNT(*) FROM post p
            WHERE UPPER(p.title) LIKE UPPER(:title)
            AND p.approved = :approved 
            AND p.tags && cast(:tags as text[])
            """)
    Mono<Long> countAllByTitleContainingIgnoreCaseAndApprovedAndTagsIn(String title, boolean approved, String tags);

    @Query("""
            SELECT * FROM post
            WHERE EXTRACT(MONTH FROM created_at) = :month
            AND EXTRACT(YEAR FROM created_at) = :year
            ORDER BY created_at DESC
            """)
    Flux<Post> findModelByMonth(int month, int year);


    @Query("""
            with emb as(
            select p.*,
                   pe.embedding
                   from post p
            join post_embedding pe on p.id = pe.entity_id)
            select
                (sub.ip+sub.ti)/2 as similarity,
                sub.*
                   from (
            select
                -(e1.embedding <#> e2.embedding) as ip,
                coalesce(
                        (
                            SELECT count(x.tag)
                            FROM unnest(e1.tags) x(tag)
                                     JOIN unnest(e2.tags) y(tag) ON x.tag = y.tag
                        )::float, 0
                ) /cardinality(e1.tags) as ti, -- gandeste mai bine decat 
                e2.*
                   from
                         emb e1, emb e2
            where e1.id=:id
             and (e2.approved=true or e2.id=e1.id)
            ) as sub
            where ((sub.ip+sub.ti)/2) >= :minSimilarity
            order by  similarity desc
            limit :limit
            """)
    Flux<PostWithSimilarity> getSimilarPosts(Long id, int limit, Double minSimilarity);
}

package com.mocicarazvan.postservice.repositories;


import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.templatemodule.repositories.ApprovedRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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

}

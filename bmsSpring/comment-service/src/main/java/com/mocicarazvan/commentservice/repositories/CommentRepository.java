package com.mocicarazvan.commentservice.repositories;


import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.commentservice.models.Comment;
import com.mocicarazvan.templatemodule.repositories.TitleBodyRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentRepository extends TitleBodyRepository<Comment> {

    Flux<Comment> findAllByReferenceIdAndReferenceType(Long referenceId, CommentReferenceType referenceType, PageRequest pageRequest);

    Mono<Long> countAllByReferenceIdAndReferenceType(Long referenceId, CommentReferenceType referenceType);

    Flux<Comment> findAllByUserIdAndReferenceType(Long userId, CommentReferenceType referenceType, PageRequest pageRequest);


    Mono<Long> countAllByUserIdAndReferenceType(Long userId, CommentReferenceType referenceType);

    Mono<Long> countAllByUserId(Long userId);

    Flux<Comment> findAllByReferenceIdAndReferenceType(Long referenceId, CommentReferenceType referenceType);

    Mono<Void> deleteAllByReferenceIdEqualsAndReferenceType(Long referenceId, CommentReferenceType referenceType);

    Flux<Comment> findAllByReferenceIdEqualsAndReferenceType(Long referenceId, CommentReferenceType referenceType);

    @Query("""
            SELECT * FROM comment
            WHERE EXTRACT(MONTH FROM created_at) = :month
            AND EXTRACT(YEAR FROM created_at) = :year
            ORDER BY created_at DESC
            """)
    Flux<Comment> findModelByMonth(int month, int year);
}

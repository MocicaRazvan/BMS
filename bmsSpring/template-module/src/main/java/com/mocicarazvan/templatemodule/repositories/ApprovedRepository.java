package com.mocicarazvan.templatemodule.repositories;


import com.mocicarazvan.templatemodule.models.Approve;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface ApprovedRepository<T extends Approve> extends TitleBodyImagesRepository<T> {
    Flux<T> findAllByApproved(boolean approved, PageRequest request);

    Flux<T> findAllByTitleContainingIgnoreCaseAndApproved(String title, boolean approved, PageRequest pageRequest);

    Mono<Long> countAllByTitleContainingIgnoreCaseAndApproved(String title, boolean approved);

    Mono<Long> countByApproved(boolean approved);

    Mono<Long> countByUserId(Long userId);

    Mono<T> findByApprovedAndId(boolean approved, Long id);

    Mono<Boolean> existsByIdAndApprovedIsTrue(Long id);

    Flux<T> findAllByUserIdAndTitleContainingIgnoreCase(Long userId, String title, PageRequest pageRequest);

    Flux<T> findAllByUserIdAndTitleContainingIgnoreCaseAndApproved(Long userId, String title, boolean approved, PageRequest pageRequest);

    Mono<Long> countAllByUserIdAndTitleContainingIgnoreCase(Long userId, String title);

    Mono<Long> countAllByUserIdAndTitleContainingIgnoreCaseAndApproved(Long userId, String title, boolean approved);

    Flux<T> findAllByTitleContainingIgnoreCase(String title, PageRequest pageRequest);

    Mono<Long> countAllByTitleContainingIgnoreCase(String title);

}

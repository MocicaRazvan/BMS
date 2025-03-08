package com.mocicarazvan.fileservice.repositories;

import com.mocicarazvan.fileservice.models.Media;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MediaRepository extends ReactiveMongoRepository<Media, String> {
    Flux<Media> findAllByGridFsIdIn(List<String> gridFsIds);

    Mono<Void> deleteAllByGridFsIdIn(List<String> gridFsIds);

    Flux<Media> findAllByGridFsId(String gridFsId);

    Mono<Void> deleteAllByGridFsId(String gridFsId);

    Mono<Void> deleteAllByToBeDeletedIsTrue();

    Mono<Long> countAllByToBeDeletedIsTrue();

    Flux<Media> findAllByToBeDeletedIsTrue();

}

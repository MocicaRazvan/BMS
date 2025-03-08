package com.mocicarazvan.fileservice.repositories;

import com.mocicarazvan.fileservice.dtos.ToBeDeletedCounts;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ExtendedMediaRepository {
    Mono<Long> markToBeDeletedByGridFsIds(List<String> gridFsIds);

    Mono<ToBeDeletedCounts> countAllByToBeDeletedIsTrue();
}

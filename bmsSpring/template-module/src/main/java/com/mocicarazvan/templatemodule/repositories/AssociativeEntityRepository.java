package com.mocicarazvan.templatemodule.repositories;


import reactor.core.publisher.Mono;

import java.util.Collection;

public interface AssociativeEntityRepository {

    Mono<Long> insertForMasterAndChildren(Long masterId, Collection<Long> childIds);

    Mono<Long> consensusChildrenForMaster(Long masterId, Collection<Long> newChildIds);

    Mono<Long> addChild(Long masterId, Long childId);

    Mono<Long> removeChild(Long masterId, Long childId);
}

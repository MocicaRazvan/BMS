package com.mocicarazvan.templatemodule.repositories;

import reactor.core.publisher.Mono;

public interface CountInParent {
    //    @Query("""
//                select count(*) from order_custom o
//                where :trainingId = any (o.trainings)
//            """)
    Mono<Long> countInParent(Long childId);
}

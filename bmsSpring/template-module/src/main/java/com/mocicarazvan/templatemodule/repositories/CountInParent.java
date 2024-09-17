package com.mocicarazvan.templatemodule.repositories;

import reactor.core.publisher.Flux;

public interface CountInParent {
    //    @Query("""
//                select count(*) from order_custom o
//                where :trainingId = any (o.trainings)
//            """)
    Flux<Long> countInParent(Long childId);
}

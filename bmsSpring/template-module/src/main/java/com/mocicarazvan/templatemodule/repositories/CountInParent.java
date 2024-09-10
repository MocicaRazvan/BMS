package com.mocicarazvan.templatemodule.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CountInParent {
    //    @Query("""
//                select count(*) from order_custom o
//                where :trainingId = any (o.trainings)
//            """)
    Flux<Long> countInParent(Long childId);
}

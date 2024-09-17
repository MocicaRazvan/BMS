package com.mocicarazvan.templatemodule.cache;

import com.mocicarazvan.templatemodule.cache.keys.ChildFilterKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface FilteredListCaffeineCacheChildFilterKey<R>
        extends FilterCaffeineCacheKeyTypeWithExtra<ChildFilterKey, Long, R> {
    Predicate<ChildFilterKey> byMasterPredicate(Long masterId);

    Predicate<ChildFilterKey> updateDeleteByMasterPredicate(Long id, Long masterId, Long userId);

    Predicate<ChildFilterKey> createByMasterPredicate(Long masterId, Long userId);

    Predicate<ChildFilterKey> byMasterAndIds(Long masterId);

    <T> Mono<T> getExtraUniqueMonoCacheForMasterIndependentOfRouteType(List<Object> attributes, String uniqueGiver,
                                                                       Function<T, Long> putIdCall,
                                                                       Long extra, Mono<T> mono);

    <T> Flux<T> getExtraUniqueFluxCacheForMasterIndependentOfRouteType(List<Object> attributes, String uniqueGiver,
                                                                       Function<T, Long> putIdCall,
                                                                       Long extra, Flux<T> flux);
}

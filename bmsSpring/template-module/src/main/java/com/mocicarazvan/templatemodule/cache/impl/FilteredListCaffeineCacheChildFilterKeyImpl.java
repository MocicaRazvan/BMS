package com.mocicarazvan.templatemodule.cache.impl;

import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheChildFilterKey;
import com.mocicarazvan.templatemodule.cache.keys.ChildFilterKey;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class FilteredListCaffeineCacheChildFilterKeyImpl<R>
        extends FilterCaffeineCacheKeyTypeWithExtraImpl<Long, Long, ChildFilterKey, R>
        implements FilteredListCaffeineCacheChildFilterKey<R> {
    private static final Long independentExtraOfMap = -100L;
    private static final Long invalidateOnCreate = -123L;

    public FilteredListCaffeineCacheChildFilterKeyImpl(String cacheBaseKey) {
        super(cacheBaseKey, -1L, independentExtraOfMap, invalidateOnCreate);
    }

    public FilteredListCaffeineCacheChildFilterKeyImpl(String cacheBaseKey, Integer cacheExpirationTimeMinutes, Integer cacheMaximumSize) {
        super(cacheBaseKey, cacheExpirationTimeMinutes, cacheMaximumSize, -1L, independentExtraOfMap, invalidateOnCreate);
    }

    @Override
    public Predicate<FilterKeyType> updateDeleteBasePredicate(Long id, Long userId) {
        return k -> true;
    }

    @Override
    public Predicate<FilterKeyType> createBasePredicate(Long userId) {
        return k -> true;
    }

    @Override
    protected ChildFilterKey createKey(String key, FilterKeyType.KeyRouteType keyRouteType) {
        return new ChildFilterKey(key, keyRouteType, defaultMap);
    }

    @Override
    protected ChildFilterKey createKey(List<Long> ids, String key, FilterKeyType.KeyRouteType keyRouteType) {
        return new ChildFilterKey(ids, key, keyRouteType, defaultMap);
    }


    @Override
    protected ChildFilterKey createKey(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType, Long extra) {
        return new ChildFilterKey(ids, cacheBaseKey, keyRouteType, extra);
    }

    @Override
    protected ChildFilterKey createKeyIndependentOfExtra(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType) {
        ChildFilterKey key = new ChildFilterKey();
        key.setIds(ids);
        key.setKey(cacheBaseKey);
        key.setRouteType(keyRouteType);
        key.setActualExtra(independentExtraOfMap);
        return key;
    }


    @Override
    public Predicate<ChildFilterKey> byMasterPredicate(Long masterId) {
        return key -> key.getExtra() == null || key.getExtra().equals(masterId) || key.getExtra().equals(defaultMap);
    }

    @Override
    public Predicate<ChildFilterKey> updateDeleteByMasterPredicate(Long id, Long masterId, Long userId) {
        return combinePredicatesOr(
                createByMasterPredicate(masterId, userId),
                idContainingPredicate(id)
        );
    }

    @Override
    public Predicate<ChildFilterKey> createByMasterPredicate(Long masterId, Long userId) {
        return combinePredicatesOr(
                byTrainerIdPredicate(userId),
                byAdminPredicate(),
                byPublicPredicate(),
                byMasterPredicate(masterId)
        );
    }


    @Override
    public Predicate<ChildFilterKey> byMasterAndIds(Long masterId) {
        return baseCaffeineCacher.getKeysByCriteria(
                        k -> k.getExtra().equals(masterId)
                ).parallelStream().map(FilterKeyType::getIds).flatMap(List::parallelStream).distinct().
                reduce(byMasterPredicate(masterId),
                        (predicate, id) -> predicate.or(idContainingPredicate(id)),
                        Predicate::or);
    }


    @Override
    public <T> Mono<T> getExtraUniqueMonoCacheForMasterIndependentOfRouteType(List<Object> attributes, String uniqueGiver,
                                                                              Function<T, Long> putIdCall,
                                                                              Long extra, Mono<T> mono) {
        return getExtraUniqueMonoCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createIndependent(), extra, mono);
    }

    @Override
    public <T> Flux<T> getExtraUniqueFluxCacheForMasterIndependentOfRouteType(List<Object> attributes, String uniqueGiver,
                                                                              Function<T, Long> putIdCall,
                                                                              Long extra, Flux<T> flux) {
        return getExtraUniqueFluxCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createIndependent(), extra, flux);
    }

}

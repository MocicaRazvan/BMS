package com.mocicarazvan.templatemodule.cache.impl;

import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCache;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;

import java.util.List;
import java.util.function.Predicate;

public class FilteredListCaffeineCacheBaseImpl<R> extends FilteredListCaffeineCacheImpl<FilterKeyType, R>
        implements FilteredListCaffeineCache<FilterKeyType, R> {
    public FilteredListCaffeineCacheBaseImpl(String cacheBaseKey) {
        super(cacheBaseKey);
    }

    public FilteredListCaffeineCacheBaseImpl(String cacheBaseKey, Integer cacheExpirationTimeMinutes, Integer cacheMaximumSize) {
        super(cacheBaseKey, cacheExpirationTimeMinutes, cacheMaximumSize);
    }

    @Override
    protected FilterKeyType createKey(String key, FilterKeyType.KeyRouteType keyRouteType) {
        return new FilterKeyType(key, keyRouteType);
    }

    @Override
    protected FilterKeyType createKey(List<Long> ids, String key, FilterKeyType.KeyRouteType keyRouteType) {
        return new FilterKeyType(ids, key, keyRouteType);
    }

    @Override
    public Predicate<FilterKeyType> updateDeleteBasePredicate(Long id, Long userId) {
        return combinePredicatesOr(
                createBasePredicate(userId),
                idContainingPredicate(id)
        );
    }

    @Override
    public Predicate<FilterKeyType> createBasePredicate(Long userId) {
        return combinePredicatesOr(
                byTrainerIdPredicate(userId),
                byAdminPredicate(),
                byPublicPredicate()
        );
    }
}

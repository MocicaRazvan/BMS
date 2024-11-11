package com.mocicarazvan.templatemodule.cache.impl;


import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheApproveFilterKey;
import com.mocicarazvan.templatemodule.cache.keys.ApproveFilterKey;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Getter
public class FilteredListCaffeineCacheApproveFilterKeyImpl<R extends ApproveDto>
        extends FilterCaffeineCacheKeyTypeWithExtraImpl<String, Boolean, ApproveFilterKey, R>
        implements FilteredListCaffeineCacheApproveFilterKey<R> {

    private static final String independentExtraOfMap = "independentExtraOfMap";
    private static final String invalidateOnCreate = "invalidateOnCreate";

    public FilteredListCaffeineCacheApproveFilterKeyImpl(String cacheBaseKey) {
        super(cacheBaseKey, null, independentExtraOfMap, invalidateOnCreate);
    }

    public FilteredListCaffeineCacheApproveFilterKeyImpl(String cacheBaseKey, Integer cacheExpirationTimeMinutes, Integer cacheMaximumSize) {
        super(cacheBaseKey, cacheExpirationTimeMinutes, cacheMaximumSize, null, independentExtraOfMap, invalidateOnCreate);
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
    protected ApproveFilterKey createKey(String key, FilterKeyType.KeyRouteType keyRouteType) {
        return new ApproveFilterKey(key, keyRouteType, defaultMap);
    }

    @Override
    protected ApproveFilterKey createKey(List<Long> ids, String key, FilterKeyType.KeyRouteType keyRouteType) {
        return new ApproveFilterKey(ids, key, keyRouteType, defaultMap);
    }


    @Override
    protected ApproveFilterKey createKey(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType, Boolean extra) {
        return new ApproveFilterKey(ids, cacheBaseKey, keyRouteType, extra);
    }

    @Override
    protected ApproveFilterKey createKeyIndependentOfExtra(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType) {
        ApproveFilterKey key = new ApproveFilterKey();
        key.setIds(ids);
        key.setKey(cacheBaseKey);
        key.setRouteType(keyRouteType);
        key.setActualExtra(independentExtraOfMap);
        return key;
    }


    @Override
    public Predicate<ApproveFilterKey> approvedDeletePredicate(Boolean approved) {
//        log.error("approvedDeletePredicate: {}", approved);
        return key -> key.getExtra() == null || key.getExtra().equals(key.mapToExtra(approved)) || key.getExtra().equals(key.mapToExtra(null));

    }

    @Override
    public Predicate<ApproveFilterKey> approvedCreatePredicate() {
//        log.error("approvedCreatePredicate");
        return approvedDeletePredicate(false);

    }

    @Override
    public Predicate<ApproveFilterKey> approvedUpdatePredicate(Boolean approved) {
//        log.error("approvedUpdatePredicate: {}", approved);
        // if approved then will move to dissaproved so both and null should be invalidated,
        // if not approved then only the dissaproved and null
        if (approved == null || approved.equals(Boolean.TRUE)) {
            return key -> true;
        }
        return approvedCreatePredicate();
    }

    @Override
    public Predicate<ApproveFilterKey> approvePredicateForPublic(boolean approved) {
//        log.error("approvePredicateForPublic: {}", approved);
        if (approved) {
            return byPublicPredicate();
        }
        return key -> false;
    }

    @Override
    public Predicate<ApproveFilterKey> createApprovedPredicateFinal(Long trainerId) {
        return combinePredicatesOr(
                combinePredicatesAnd(
                        approvedCreatePredicate(),
                        byTrainerIdPredicate(trainerId)
                ),
                combinePredicatesAnd(
                        approvedCreatePredicate(),
                        byAdminPredicate()
                )
        );
    }

    @Override
    public Predicate<ApproveFilterKey> changeApprovedStatusPredicateFinal(Long id, boolean approved, R r) {
        return combinePredicatesOr(
                byTrainerIdPredicate(r.getUserId()),
                byAdminPredicate(),
                byPublicPredicate(),
                idContainingPredicate(id)
        );
    }

    @Override
    public Predicate<ApproveFilterKey> deleteApprovedPredicateFinal(Long id, R r) {
        return combinePredicatesOr(
                combinePredicatesAnd(
                        byTrainerIdPredicate(r.getUserId()),
                        approvedDeletePredicate(r.isApproved())
                ),
                combinePredicatesAnd(
                        byAdminPredicate(),
                        approvedDeletePredicate(r.isApproved())
                ),
                approvePredicateForPublic(r.isApproved()),
                idContainingPredicate(id)
        );
    }

    @Override
    public Predicate<ApproveFilterKey> updateApprovedPredicateFinal(Pair<R, Boolean> pr) {
        return combinePredicatesOr(
                combinePredicatesAnd(
                        byTrainerIdPredicate(pr.getFirst().getUserId()),
                        approvedUpdatePredicate(pr.getSecond())
                ),
                combinePredicatesAnd(
                        byAdminPredicate(),
                        approvedUpdatePredicate(pr.getSecond())
                ),
                approvePredicateForPublic(pr.getSecond()),
                idContainingPredicate(pr.getFirst().getId())
        );
    }

}

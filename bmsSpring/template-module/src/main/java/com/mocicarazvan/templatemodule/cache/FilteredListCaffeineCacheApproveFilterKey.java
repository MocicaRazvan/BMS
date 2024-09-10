package com.mocicarazvan.templatemodule.cache;

import com.mocicarazvan.templatemodule.cache.keys.ApproveFilterKey;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import org.springframework.data.util.Pair;

import java.util.function.Predicate;

public interface FilteredListCaffeineCacheApproveFilterKey<R
        extends ApproveDto
        > extends FilterCaffeineCacheKeyTypeWithExtra<ApproveFilterKey, Boolean, R> {
    Predicate<ApproveFilterKey> approvedUpdatePredicate(Boolean approved);

    Predicate<ApproveFilterKey> approvedDeletePredicate(Boolean approved);

    Predicate<ApproveFilterKey> approvedCreatePredicate();

    Predicate<ApproveFilterKey> approvePredicateForPublic(boolean approved);

    Predicate<ApproveFilterKey> createApprovedPredicateFinal(Long trainerId);

    Predicate<ApproveFilterKey> changeApprovedStatusPredicateFinal(Long id, boolean approved, R r);

    Predicate<ApproveFilterKey> deleteApprovedPredicateFinal(Long id, R r);

    Predicate<ApproveFilterKey> updateApprovedPredicateFinal(Pair<R, Boolean> pr);
}

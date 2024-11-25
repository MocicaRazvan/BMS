//package com.mocicarazvan.templatemodule.adapters;
//
//import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheApproveFilterKey;
//import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
//import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
//import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyDto;
//import com.mocicarazvan.templatemodule.models.Approve;
//import com.mocicarazvan.templatemodule.models.IdGenerated;
//import com.mocicarazvan.templatemodule.services.impl.ApprovedServiceImpl;
//import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
//import lombok.Builder;
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//
//@RequiredArgsConstructor
//@Builder
//@Data
//public class CacheApprovedFilteredToHandlerAdapter {
//    public static <M extends Approve, B extends TitleBodyDto, R extends ApproveDto, APP extends ApprovedServiceImpl.ApprovedServiceCacheHandler<M, B, R>> APP
//    convert(FilteredListCaffeineCacheApproveFilterKey<R> cacheFilter,
//            APP approvedServiceCacheHandler) {
//        approvedServiceCacheHandler.setCreateModelInvalidate(
//                ((mono, body, userId) -> cacheFilter.invalidateByWrapper(mono,
//                        cacheFilter.createApprovedPredicateFinal(Long.valueOf(userId))
//                )
//                ));
//
//        approvedServiceCacheHandler.setGetModelGroupedByMonthPersist(
//                ((monthlyEntityGroupFlux, userDto, month) ->
//                        cacheFilter.getExtraUniqueCacheForAdmin(
//                                EntitiesUtils.getListOfNotNullObjects(monthlyEntityGroupFlux, userDto, month),
//                                "getMonthlyGroupedPersist" + month,
//                                rm -> rm.getEntity().getId(),
//                                null,
//                                monthlyEntityGroupFlux
//                        )
//                )
//        );
//
//        approvedServiceCacheHandler.setApproveModelInvalidate(
//                (mono, id, userId, approved) -> cacheFilter.invalidateByWrapperCallback(mono, r -> cacheFilter.changeApprovedStatusPredicateFinal(id, approved, r.getModel()))
//        );
//
//        approvedServiceCacheHandler.setDeleteModelInvalidate(
//                (mono, id, userId) -> cacheFilter.invalidateByWrapperCallback(mono, r -> cacheFilter.deleteApprovedPredicateFinal(id, r))
//        );
//
//        approvedServiceCacheHandler.setGetModelByIdPersist(
//                (mono, authUser, id) -> cacheFilter.getExtraUniqueMonoCacheIndependent(
//                        EntitiesUtils.getListOfNotNullObjects(id),
//                        "getByIdPersist" + id,
//                        IdGenerateDto::getId,
//                        mono
//                )
//        );
//
//        approvedServiceCacheHandler.setGetByIdInternalPersist(
//                (mono, id) -> cacheFilter.getExtraUniqueMonoCacheIndependent(
//                        EntitiesUtils.getListOfNotNullObjects(id),
//                        "getByIdInternalPersist" + id,
//                        IdGenerated::getId,
//                        mono
//                )
//        );
//
//        approvedServiceCacheHandler.setGetModelByIdWithUserPersist(
//                (mono, authUser, id) -> cacheFilter.getExtraUniqueMonoCacheIndependent(
//                        EntitiesUtils.getListOfNotNullObjects(id),
//                        "getByIdWithUserPersist" + id,
//                        m -> m.getModel().getId(),
//                        mono
//                )
//        );
//
//        approvedServiceCacheHandler.setUpdateModelInvalidate(
//                (mono, id, body, userId) -> cacheFilter.invalidateAllCache(mono)
//        );
//        approvedServiceCacheHandler.setReactToModelInvalidate(
//                (mono, id, type, userId) -> cacheFilter.invalidateByWrapperCallback(mono, _ -> cacheFilter.idContainingPredicate(id))
//        );
//
//        approvedServiceCacheHandler.setUpdateModelGetOriginalApprovedInvalidate(
//                (mono, id, body, userId) -> cacheFilter.invalidateByWrapperCallback(mono, cacheFilter::updateApprovedPredicateFinal)
//        );
//
//
//        return approvedServiceCacheHandler;
//    }
//}

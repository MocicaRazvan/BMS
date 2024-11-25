//package com.mocicarazvan.templatemodule.adapters;
//
//import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheChildFilterKey;
//import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
//import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
//import com.mocicarazvan.templatemodule.models.IdGenerated;
//import com.mocicarazvan.templatemodule.models.ManyToOneUser;
//import com.mocicarazvan.templatemodule.models.TitleBody;
//import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
//import com.mocicarazvan.templatemodule.services.impl.TitleBodyServiceImpl;
//import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
//import reactor.core.publisher.Mono;
//
//import java.util.function.Function;
//
//public class CacheChildFilteredToHandlerAdapter {
//
//    public static <MODEL extends ManyToOneUser, BODY, RESPONSE extends WithUserDto, MSC extends
//            ManyToOneUserServiceImpl.ManyToOneUserServiceCacheHandler<MODEL, BODY, RESPONSE>>
//    MSC convertToManyUserHandler(
//            FilteredListCaffeineCacheChildFilterKey<RESPONSE> cacheFilter,
//            MSC manyToOneUserServiceCacheHandler,
//            Function<RESPONSE, Long> getMasterId,
//            Function<MODEL, RESPONSE> mapper
//    ) {
//
//
//        manyToOneUserServiceCacheHandler.setGetModelGroupedByMonthPersist(((monthlyEntityGroupFlux, userDto, month) ->
//                cacheFilter.getExtraUniqueCacheForAdmin(
//                        EntitiesUtils.getListOfNotNullObjects(userDto, month),
//                        "getModelGroupedByMonthPersist" + month,
//                        rm -> rm.getEntity().getId(),
//                        cacheFilter.getDefaultMap(),
//                        monthlyEntityGroupFlux
//                )
//        ));
//
//        manyToOneUserServiceCacheHandler.setGetModelByIdPersist((mono, authUser, id) ->
//                mono.flatMap(
//                        model -> cacheFilter.getExtraUniqueMonoCacheForMasterIndependentOfRouteType(
//                                EntitiesUtils.getListOfNotNullObjects(id),
//                                "getModelByIdPersist" + id,
//                                IdGenerateDto::getId,
//                                getMasterId.apply(model),
//                                Mono.just(model)
//                        )
//                )
//        );
//
//        manyToOneUserServiceCacheHandler.setGetByIdInternalPersist((mono, id) ->
//                mono.flatMap(
//                        model -> cacheFilter.getExtraUniqueMonoCacheForMasterIndependentOfRouteType(
//                                EntitiesUtils.getListOfNotNullObjects(id),
//                                "getByIdInternalPersist" + id,
//                                IdGenerated::getId,
//                                getMasterId.apply(mapper.apply(model)),
//                                Mono.just(model)
//                        )
//                )
//        );
//
//        manyToOneUserServiceCacheHandler.setGetModelByIdWithUserPersist((mono, authUser, id) ->
//                mono.flatMap(
//                        model -> cacheFilter.getExtraUniqueMonoCacheForMasterIndependentOfRouteType(
//                                EntitiesUtils.getListOfNotNullObjects(id),
//                                "getModelByIdWithUserPersist" + id,
//                                m -> m.getModel().getId(),
//                                getMasterId.apply(model.getModel()),
//                                Mono.just(model)
//                        )));
//
//
//        manyToOneUserServiceCacheHandler.setDeleteModelInvalidate(
//                (mono, id, userId) -> cacheFilter.invalidateByWrapperCallback(mono, r ->
//                        cacheFilter.updateDeleteByMasterPredicate(id, getMasterId.apply(r), r.getUserId()))
//        );
//
//        manyToOneUserServiceCacheHandler.setUpdateModelInvalidate(
//                (mono, id, body, userId) -> cacheFilter.invalidateByWrapperCallback(mono, r ->
//                        cacheFilter.updateDeleteByMasterPredicate(id, getMasterId.apply(r), r.getUserId()))
//        );
//
//        manyToOneUserServiceCacheHandler.setCreateModelInvalidate(
//                (mono, body, userId) -> cacheFilter.invalidateByWrapperCallback(mono, r ->
//                        cacheFilter.createByMasterPredicate(getMasterId.apply(r), r.getUserId())
//                ));
//
//
//        return manyToOneUserServiceCacheHandler;
//    }
//
//    public static <MODEL extends TitleBody, BODY, RESPONSE extends WithUserDto, TSC extends
//            TitleBodyServiceImpl.TitleBodyServiceCacheHandler<MODEL, BODY, RESPONSE>>
//    TSC convertToTitleBodyHandler(
//            FilteredListCaffeineCacheChildFilterKey<RESPONSE> cacheFilter,
//            TSC titleBodyServiceCacheHandler,
//            Function<RESPONSE, Long> getMasterId,
//            Function<MODEL, RESPONSE> mapper
//
//    ) {
//        CacheChildFilteredToHandlerAdapter.convertToManyUserHandler(cacheFilter, titleBodyServiceCacheHandler, getMasterId, mapper);
//
//        titleBodyServiceCacheHandler.setReactToModelInvalidate(
//                (mono, id, type, userId) -> cacheFilter.invalidateByWrapperCallback(mono, r ->
//                        cacheFilter.updateDeleteByMasterPredicate(id, getMasterId.apply(r), r.getUserId()))
//        );
//
//        return titleBodyServiceCacheHandler;
//    }
//}

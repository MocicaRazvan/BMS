package com.mocicarazvan.templatemodule.adapters;

import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCache;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import com.mocicarazvan.templatemodule.models.IdGenerated;
import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import com.mocicarazvan.templatemodule.models.TitleBody;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.services.impl.TitleBodyServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;

public class CacheBaseFilteredToHandlerAdapter {

    public static <MODEL extends ManyToOneUser, BODY, RESPONSE extends WithUserDto, MSC extends
            ManyToOneUserServiceImpl.ManyToOneUserServiceCacheHandler<MODEL, BODY, RESPONSE>>
    MSC convertToManyUserHandler(
            FilteredListCaffeineCache<FilterKeyType, RESPONSE> cacheFilter,
            MSC manyToOneUserServiceCacheHandler
    ) {


        manyToOneUserServiceCacheHandler.setGetModelGroupedByMonthPersist(
                ((monthlyEntityGroupFlux, userDto, month) ->
                        cacheFilter.getUniqueFluxCacheForAdmin(
                                EntitiesUtils.getListOfNotNullObjects(userDto, month),
                                "getModelGroupedByMonthPersist" + month,
                                rm -> rm.getEntity().getId(),
                                monthlyEntityGroupFlux
                        )
                )
        );


        manyToOneUserServiceCacheHandler.setGetModelByIdPersist(
                (mono, authUser, id) -> cacheFilter.getUniqueMonoCacheIndependent(
                        EntitiesUtils.getListOfNotNullObjects(id),
                        "getModelByIdPersist" + id,
                        IdGenerateDto::getId,
                        mono
                )
        );

        manyToOneUserServiceCacheHandler.setGetByIdInternalPersist(
                (mono, id) -> cacheFilter.getUniqueMonoCacheIndependent(
                        EntitiesUtils.getListOfNotNullObjects(id),
                        "getByIdInternalPersist" + id,
                        IdGenerated::getId,
                        mono
                )
        );

        manyToOneUserServiceCacheHandler.setGetModelByIdWithUserPersist(
                (mono, authUser, id) -> cacheFilter.getUniqueMonoCacheIndependent(
                        EntitiesUtils.getListOfNotNullObjects(id),
                        "getModelByIdWithUserPersist" + id,
                        m -> m.getModel().getId(),
                        mono
                )
        );


        manyToOneUserServiceCacheHandler.setDeleteModelInvalidate(
                (mono, id, userId) ->
                        cacheFilter.invalidateByWrapperCallback(
                                mono, r -> cacheFilter.updateDeleteBasePredicate(id, r.getUserId())
                        )
        );

        manyToOneUserServiceCacheHandler.setUpdateModelInvalidate(
                (mono, id, body, userId) ->
                        cacheFilter.invalidateByWrapperCallback(
                                mono, r -> cacheFilter.updateDeleteBasePredicate(id, r.getUserId())
                        )
        );

        manyToOneUserServiceCacheHandler.setCreateModelInvalidate(
                (mono, body, userId) ->
                        cacheFilter.invalidateByWrapperCallback(
                                mono, r -> cacheFilter.createBasePredicate(r.getUserId())
                        )
        );

        return manyToOneUserServiceCacheHandler;
    }

    public static <MODEL extends TitleBody, BODY, RESPONSE extends WithUserDto, TSC extends
            TitleBodyServiceImpl.TitleBodyServiceCacheHandler<MODEL, BODY, RESPONSE>>
    TSC convertToTitleBodyHandler(
            FilteredListCaffeineCache<FilterKeyType, RESPONSE> cacheFilter,
            TSC titleBodyServiceCacheHandler
    ) {
        CacheBaseFilteredToHandlerAdapter.convertToManyUserHandler(cacheFilter, titleBodyServiceCacheHandler);

        titleBodyServiceCacheHandler.setReactToModelInvalidate(
                (mono, id, body, userId) ->
                        cacheFilter.invalidateByWrapperCallback(
                                mono, r -> cacheFilter.updateDeleteBasePredicate(id, r.getUserId())
                        )
        );

        return titleBodyServiceCacheHandler;
    }
}

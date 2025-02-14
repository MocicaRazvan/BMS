package com.mocicarazvan.orderservice.cache;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Getter
@RequiredArgsConstructor
public class OrderWithAddressCacheHandler {
//    private final FilteredListCaffeineCacheChildFilterKey<OrderDtoWithAddress> cacheFilter;
//
//    public Mono<Void> createOrderWithAddressInvalidate(String userId) {
//        return cacheFilter.invalidateByVoid(
//                cacheFilter.createByMasterPredicate(Long.valueOf(userId), Long.valueOf(userId))
//        );
//    }
//
//    public Mono<OrderDtoWithAddress> getModelByIdPersist(Mono<OrderDtoWithAddress> mono,
//                                                         Long id, UserDto userDto) {
//        return mono.flatMap(
//                oa -> cacheFilter.getExtraUniqueMonoCacheForMasterIndependentOfRouteType(
//                        EntitiesUtils.getListOfNotNullObjects(id, userDto),
//                        "getModelByIdPersist" + id,
//                        o -> o.getOrder().getId(),
//                        oa.getOrder().getUserId(),
//                        Mono.just(oa)
//                )
//
//        );
//    }
//
//    public Flux<PageableResponse<OrderDtoWithAddress>> getModelsFilteredAdminPersist(
//            Flux<PageableResponse<OrderDtoWithAddress>> flux,
//            String city, String state, String country, PageableBody pageableBody
//    ) {
//        return cacheFilter.getExtraUniqueCacheForAdmin(
//                EntitiesUtils.getListOfNotNullObjects(city, state, country, pageableBody),
//                "getModelsFilteredAdminPersist",
//                o -> o.getContent().getOrder().getId(),
//                cacheFilter.getDefaultMap(),
//                flux
//        );
//    }
//
//    public Flux<PageableResponse<OrderDtoWithAddress>> getModelsFilteredUser(
//            Flux<PageableResponse<OrderDtoWithAddress>> flux,
//            String city,
//            String state,
//            String country,
//            PageableBody pageableBody,
//            Long userId,
//            UserDto userDto) {
//
//        return cacheFilter.getExtraUniqueFluxCacheForMasterIndependentOfRouteType(
//                EntitiesUtils.getListOfNotNullObjects(city, state, country, pageableBody, userId, userDto),
//                "getModelsFilteredUser" + userId,
//                o -> o.getContent().getOrder().getId(),
//                userId,
//                flux
//        );
//    }
}
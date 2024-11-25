package com.mocicarazvan.orderservice.services.impl;

import com.mocicarazvan.orderservice.cache.OrderWithAddressCacheHandler;
import com.mocicarazvan.orderservice.dtos.OrderDtoWithAddress;
import com.mocicarazvan.orderservice.mappers.OrderWithAddressMapper;
import com.mocicarazvan.orderservice.repositories.ExtendedOrderWithAddressRepository;
import com.mocicarazvan.orderservice.services.OrderWithAddressService;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCache;
import com.mocicarazvan.rediscache.annotation.RedisReactiveChildCacheEvict;
import com.mocicarazvan.templatemodule.clients.UserClient;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderWithAddressServiceImpl implements OrderWithAddressService {


    private static final String NAME = "orderWithAddress";

    private static final List<String> allowedFields = List.of("id", "createdAt", "updatedAt", "total");

    private final OrderWithAddressMapper orderWithAddressMapper;

    private final ExtendedOrderWithAddressRepository extendedOrderWithAddressRepository;

    private final UserClient userClient;
    private final PageableUtilsCustom pageableUtils;

    private final EntitiesUtils entitiesUtils;
    private final OrderWithAddressCacheHandler orderWithAddressCacheHandler;
    private final OrderWithAddRedisCacheWrapper self;


    @Override
    public Mono<OrderDtoWithAddress> getModelById(Long id, String userId) {

        return userClient.getUser("", userId)
                .flatMap(userDto ->
                        self.getModelByIdBase(id, userDto)

                );
    }


    @Override
    public Flux<PageableResponse<OrderDtoWithAddress>> getModelsFilteredAdmin(String city, String state, String country, PageableBody pageableBody, String userId) {

        return userClient.getUser("", userId)
                .flatMap(entitiesUtils::checkAdmin)
                .then(pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedFields))
                .then(pageableUtils.createPageRequest(pageableBody))
                .flatMapMany(pr ->
                        self.getModelsFilteredAdminBase(city, state, country, pr)
                );
    }

    @Override
    public Flux<PageableResponse<OrderDtoWithAddress>> getModelsFilteredUser(String city, String state, String country, PageableBody pageableBody, Long userId, String authUserId) {

        return userClient.getUser("", authUserId)
                .flatMapMany(userDto ->
                        self.getModelsFilteredUserBase(city, state, country, pageableBody, userId, userDto)
                );
    }

    @Getter
    @Component
    public static class OrderWithAddRedisCacheWrapper {
        private final ExtendedOrderWithAddressRepository extendedOrderWithAddressRepository;
        private final EntitiesUtils entitiesUtils;
        private final OrderWithAddressMapper orderWithAddressMapper;
        private final String CACHE_KEY_NAME = "orderWithAddress";
        private final PageableUtilsCustom pageableUtils;

        public OrderWithAddRedisCacheWrapper(ExtendedOrderWithAddressRepository extendedOrderWithAddressRepository, EntitiesUtils entitiesUtils, OrderWithAddressMapper orderWithAddressMapper, PageableUtilsCustom pageableUtils) {
            this.extendedOrderWithAddressRepository = extendedOrderWithAddressRepository;
            this.entitiesUtils = entitiesUtils;
            this.orderWithAddressMapper = orderWithAddressMapper;
            this.pageableUtils = pageableUtils;
        }


        @RedisReactiveChildCache(key = CACHE_KEY_NAME, id = "#id", masterId = "#userDto.id")
        public Mono<OrderDtoWithAddress> getModelByIdBase(Long id, UserDto userDto) {
            return
                    extendedOrderWithAddressRepository.getModelById(id)
                            .switchIfEmpty(Mono.error(new NotFoundEntity(NAME, id)))
                            .flatMap(orderWithAddress -> entitiesUtils.checkEntityOwnerOrAdmin(orderWithAddress.getOrder(), userDto)
                                    .thenReturn(orderWithAddressMapper.fromModelToDto(orderWithAddress)));


        }

        @RedisReactiveChildCache(key = CACHE_KEY_NAME, idPath = "content.order.id")
        public Flux<PageableResponse<OrderDtoWithAddress>> getModelsFilteredAdminBase(String city, String state, String country, PageRequest pr) {
            return
                    pageableUtils.createPageableResponse(
                            extendedOrderWithAddressRepository.getModelsFiltered(city, state, country, pr).map(orderWithAddressMapper::fromModelToDto),
                            extendedOrderWithAddressRepository.countModelsFiltered(city, state, country),
                            pr
                    );

        }

        @RedisReactiveChildCache(key = CACHE_KEY_NAME, idPath = "content.order.id", masterId = "#userId")
        public Flux<PageableResponse<OrderDtoWithAddress>> getModelsFilteredUserBase(String city, String state, String country, PageableBody pageableBody, Long userId, UserDto userDto) {
            return
                    pageableUtils.isSortingCriteriaValid(pageableBody.getSortingCriteria(), allowedFields)
                            .then(pageableUtils.createPageRequest(pageableBody))
                            .flatMapMany(pr -> pageableUtils.createPageableResponse(
                                    extendedOrderWithAddressRepository.getModelsFilteredUser(city, state, country, userId, pr)
                                            .flatMap(orderWithAddress -> entitiesUtils.checkEntityOwnerOrAdmin(orderWithAddress.getOrder(), userDto)
                                                    .thenReturn(orderWithAddressMapper.fromModelToDto(orderWithAddress))
                                            ),
                                    extendedOrderWithAddressRepository.countModelsFilteredUser(city, state, country, userId),
                                    pr
                            ))

                    ;
        }

        @RedisReactiveChildCacheEvict(key = CACHE_KEY_NAME, masterId = "#userId")
        public Mono<String> invalidateForMaster(String userId) {
            return Mono.just("Invalidated");
        }

    }
}

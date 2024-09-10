package com.mocicarazvan.orderservice.cache;

import com.mocicarazvan.orderservice.dtos.OrderDto;
import com.mocicarazvan.orderservice.dtos.summaries.CountryOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.DailyOrderSummary;
import com.mocicarazvan.orderservice.dtos.summaries.MonthlyOrderSummary;
import com.mocicarazvan.orderservice.enums.CountrySummaryType;
import com.mocicarazvan.orderservice.mappers.OrderMapper;
import com.mocicarazvan.orderservice.models.Order;
import com.mocicarazvan.templatemodule.adapters.CacheChildFilteredToHandlerAdapter;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCacheChildFilterKey;
import com.mocicarazvan.templatemodule.services.impl.ManyToOneUserServiceImpl;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@Getter
public class OrderCacheServiceHandler extends ManyToOneUserServiceImpl
        .ManyToOneUserServiceCacheHandler<Order, OrderDto, OrderDto> {

    private final FilteredListCaffeineCacheChildFilterKey<OrderDto> cacheFilter;
    private final OrderMapper orderMapper;
    Function2<Mono<String>, String, Mono<String>> createOrderInvalidate;
    Function3<Flux<MonthlyOrderSummary>, LocalDate, LocalDate, Flux<MonthlyOrderSummary>> getOrdersSummaryByMonthPersist;
    Function3<Flux<DailyOrderSummary>, LocalDate, LocalDate, Flux<DailyOrderSummary>> getOrdersSummaryByDayPersist;
    Function2<Flux<CountryOrderSummary>, CountrySummaryType, Flux<CountryOrderSummary>> getOrdersSummaryByCountryPersist;

    public OrderCacheServiceHandler(FilteredListCaffeineCacheChildFilterKey<OrderDto> cacheFilter, OrderMapper orderMapper) {
        super();
        this.cacheFilter = cacheFilter;
        this.orderMapper = orderMapper;
        CacheChildFilteredToHandlerAdapter.convertToManyUserHandler(
                cacheFilter, this,
                OrderDto::getUserId,
                orderMapper::fromModelToDto
        );
        this.createOrderInvalidate = (v, userId) -> cacheFilter.invalidateByWrapperCallback(v,
                n -> cacheFilter.createByMasterPredicate(Long.valueOf(userId), Long.valueOf(userId))
        );
        this.getOrdersSummaryByMonthPersist = (flux, from, to) ->
                cacheFilter.getExtraUniqueCacheForAdmin(
                        EntitiesUtils.getListOfNotNullObjects(from, to),
                        "getOrdersSummaryByMonthPersist" + from + to,
                        i -> -11L,
                        cacheFilter.getDefaultMap(),
                        flux
                );

        this.getOrdersSummaryByDayPersist = (flux, from, to) ->
                cacheFilter.getExtraUniqueCacheForAdmin(
                        EntitiesUtils.getListOfNotNullObjects(from, to),
                        "getOrdersSummaryByDayPersist" + from + to,
                        i -> -11L,
                        cacheFilter.getDefaultMap(),
                        flux
                );

        this.getOrdersSummaryByCountryPersist = (flux, type) -> cacheFilter.getExtraUniqueCacheForAdmin(
                EntitiesUtils.getListOfNotNullObjects(type),
                "getOrdersSummaryByCountryPersist" + type,
                i -> -11L,
                cacheFilter.getDefaultMap(),
                flux
        );

    }


}

package com.mocicarazvan.orderservice.cache;

import com.mocicarazvan.orderservice.dtos.summaries.CountryOrderSummary;
import com.mocicarazvan.orderservice.enums.CountrySummaryType;
import com.mocicarazvan.templatemodule.cache.BaseCaffeienCacherImpl;
import com.mocicarazvan.templatemodule.cache.BaseCaffeineCacher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OrderCacher {

    private final BaseCaffeineCacher baseCaffeineCacher;

    public OrderCacher() {
        this.baseCaffeineCacher = BaseCaffeienCacherImpl.GetBaseCaffeineCacherWithNoExpirationTime();
    }

    public Flux<CountryOrderSummary> getCachedCountryOrderSummaryFlux(CountrySummaryType type, Flux<CountryOrderSummary> flux) {
        log.info(STR."getCachedCountryOrderSummaryFlux called \{type.toString()}");
        return baseCaffeineCacher.getCachedFlux(STR."/admin/summaryByCountry\{type.toString()}", flux);
    }

    public Mono<Void> invalidateAllCache() {
        return baseCaffeineCacher.invalidateAllCache();
    }
}

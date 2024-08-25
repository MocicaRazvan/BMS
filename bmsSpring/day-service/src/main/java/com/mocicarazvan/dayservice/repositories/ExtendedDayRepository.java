package com.mocicarazvan.dayservice.repositories;

import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.dayservice.models.Day;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ExtendedDayRepository {
    Flux<Day> getDaysFiltered(String title, DayType dayType, PageRequest pageRequest, List<Long> excludeIds);

    Flux<Day> getDaysFilteredTrainer(String title, DayType dayType, PageRequest pageRequest, List<Long> excludeIds, Long trainerId);

    Flux<Day> getDaysFilteredByIds(String title, DayType dayType, List<Long> ids, List<Long> excludeIds, PageRequest pageRequest);


    Mono<Long> countDayFiltered(String title, DayType dayType, List<Long> excludeIds);

    Mono<Long> countDayFilteredTrainer(String title, DayType dayType, List<Long> excludeIds, Long trainerId);

    Mono<Long> countDayFilteredByIds(String title, DayType dayType, List<Long> ids, List<Long> excludeIds);
}

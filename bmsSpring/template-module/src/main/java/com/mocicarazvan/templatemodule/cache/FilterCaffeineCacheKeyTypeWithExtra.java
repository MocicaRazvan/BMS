package com.mocicarazvan.templatemodule.cache;

import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface FilterCaffeineCacheKeyTypeWithExtra<K extends FilterKeyType, M, R>
        extends FilteredListCaffeineCache<K, R> {

    M getDefaultMap();

    Object getIndependentExtraOfMap();

    <T> Mono<T> getExtraUniqueMonoCacheIdList(List<Object> attributes, String uniqueGiver,
                                              List<Long> ids,
                                              FilterKeyType.KeyRouteType keyRouteType,
                                              M extra, Mono<T> mono);

    <T> Mono<T> getExtraUniqueMonoCache(List<Object> attributes, String uniqueGiver,
                                        Function<T, Long> putIdCall,
                                        FilterKeyType.KeyRouteType keyRouteType,
                                        M extra, Mono<T> mono);


    <T> Mono<T> getExtraUniqueMonoCacheIndependent(List<Object> attributes, String uniqueGiver,
                                                   Function<T, Long> putIdCall,
                                                   Mono<T> mono);

    <T> Mono<T> getExtraUniqueMonoCacheForPublic(List<Object> attributes, String uniqueGiver,
                                                 Function<T, Long> putIdCall,
                                                 M extra, Mono<T> mono);

    <T> Mono<T> getExtraUniqueMonoCacheForAdmin(List<Object> attributes, String uniqueGiver,
                                                Function<T, Long> putIdCall,
                                                M extra, Mono<T> mono);

    <T> Mono<T> getExtraUniqueMonoCacheForTrainer(List<Object> attributes, Long trainerId, String uniqueGiver,
                                                  Function<T, Long> putIdCall,
                                                  M extra, Mono<T> mono);

    <T> Mono<T> getExtraUniqueMonoCacheIdListIndependent(List<Object> attributes, String uniqueGiver,
                                                         List<Long> ids,
                                                         Mono<T> mono);

    <T> Flux<T> getExtraUniqueFluxCache(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, FilterKeyType.KeyRouteType keyRouteType, M extra, Flux<T> flux);


    <T> Flux<T> getExtraUniqueFluxCacheIndependent(List<Object> attributes, String uniqueGiver,
                                                   Function<T, Long> putIdCall, Flux<T> flux);

    <T> Flux<T> getExtraUniqueCacheForTrainer(List<Object> attributes, Long trainerId, String uniqueGiver, Function<T, Long> putIdCall, M extra, Flux<T> flux);

    <T> Flux<T> getExtraUniqueCacheForAdmin(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, M extra, Flux<T> flux);

    <T> Flux<T> getExtraUniqueCacheForPublic(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, M extra, Flux<T> flux);

    Predicate<K> extraContainingPredicate(M extra);

    Mono<CustomEntityModel<R>> invalidateByWrapperEntityExtra(Mono<Pair<CustomEntityModel<R>, M>> mono, Function<Pair<R, M>, Predicate<K>> predicate);

    Mono<ResponseWithUserDtoEntity<R>> invalidateByWrapperEntityUserExtra(Mono<ResponseWithUserDtoEntity<R>> mono, Function<R, Predicate<K>> predicate);

    Predicate<K> byInvalidateOnCreatePredicate();
}

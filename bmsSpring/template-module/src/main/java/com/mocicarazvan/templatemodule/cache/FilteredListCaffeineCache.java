package com.mocicarazvan.templatemodule.cache;

import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface FilteredListCaffeineCache<K extends FilterKeyType, R> {

    <T> Mono<T> getUniqueMonoCacheIdList(List<Object> attributes, String uniqueGiver, List<Long> ids, FilterKeyType.KeyRouteType keyRouteType, Mono<T> mono);

    <T> Mono<T> getUniqueMonoCache(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, FilterKeyType.KeyRouteType keyRouteType, Mono<T> mono);

    <T> Mono<T> getUniqueMonoCacheIndependent(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, Mono<T> mono);

    <T> Mono<T> getUniqueMonoCacheIdListIndependent(List<Object> attributes, String uniqueGiver, List<Long> ids, Mono<T> mono);

    <T> Flux<T> getUniqueFluxCache(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, FilterKeyType.KeyRouteType keyRouteType, Flux<T> flux);

    <T> Flux<T> getUniqueFluxCacheIndependent(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, Flux<T> flux);

    <T> Flux<T> getUniqueFluxCacheForTrainer(List<Object> attributes, Long trainerId, String uniqueGiver, Function<T, Long> putIdCall, Flux<T> flux);

    <T> Flux<T> getUniqueFluxCacheForAdmin(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, Flux<T> flux);

    <T> Flux<T> getUniqueFluxCacheForPublic(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, Flux<T> flux);

    Predicate<K> idContainingPredicate(Long id);

    Predicate<K> idContainingPredicate(List<Long> ids);

    Predicate<K> deleteAllPredicate();

    Predicate<K> byTrainerIdPredicate(Long trainerId);

    Predicate<K> byAdminPredicate();

    Predicate<K> byPublicPredicate();

    Predicate<K> byIdAndTrainerIdPredicate(Long id, Long trainerId);

    Predicate<K> byIdAndAdminPredicate(Long id);

    Predicate<K> byIdAndPublicPredicate(Long id);


    Mono<Void> invalidateByVoid(Predicate<K> predicate);

    @SuppressWarnings("unchecked")
    Predicate<K> combinePredicatesAnd(Predicate<K>... predicates);

    @SuppressWarnings("unchecked")
    Predicate<K> combinePredicatesOr(Predicate<K>... predicates);

    <T> Flux<T> invalidateByWrapper(Flux<T> flux, Predicate<K> predicate);

    <T> Mono<T> invalidateByWrapper(Mono<T> mono, Predicate<K> predicate);

    //    protected Mono<Void> invalidateAllCacheByIdContaining(List<Long> ids) {
    //        return baseCaffeineCacher.invalidateAllCache(
    //                baseCaffeineCacher.getKeysByCriteria(key -> key.getIds().stream().anyMatch(ids::contains))
    //        );
    //    }
    Mono<Void> invalidateAllCache();

    <T> Flux<T> invalidateAllCache(Flux<T> flux);

    <T> Mono<T> invalidateAllCache(Mono<T> mono);


    <T> Mono<T> invalidateByIdFlatten(T t, Long id);

    Mono<CustomEntityModel<R>> invalidateByWrapperEntity(Mono<CustomEntityModel<R>> mono, Function<R, Predicate<K>> predicate);

    <T> Mono<T> invalidateByWrapperCallback(Mono<T> t, Function<T, Predicate<K>> predicate);

    Mono<ResponseWithUserDtoEntity<R>> invalidateByWrapperEntityUser(Mono<ResponseWithUserDtoEntity<R>> mono, Function<R, Predicate<K>> predicate);

    Predicate<FilterKeyType> updateDeleteBasePredicate(Long id, Long userId);

    Predicate<FilterKeyType> createBasePredicate(Long userId);

    BaseCaffeineCacher<K> getBaseCaffeineCacher();

    Predicate<K> byIdsList(List<Long> ids);
}

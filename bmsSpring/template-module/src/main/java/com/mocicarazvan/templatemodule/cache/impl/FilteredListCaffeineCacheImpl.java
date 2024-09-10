package com.mocicarazvan.templatemodule.cache.impl;

import com.mocicarazvan.templatemodule.cache.BaseCaffeineCacher;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.cache.FilteredListCaffeineCache;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class FilteredListCaffeineCacheImpl<K extends FilterKeyType, R> implements FilteredListCaffeineCache<K, R> {

    protected final BaseCaffeineCacher<K> baseCaffeineCacher;
    protected final String cacheBaseKey;


    protected abstract K createKey(String key, FilterKeyType.KeyRouteType keyRouteType);

    protected abstract K createKey(List<Long> ids, String key, FilterKeyType.KeyRouteType keyRouteType);


    protected <T> Flux<T> saveToCache(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType, Flux<T> cachedFlux) {
        K finalKey = createKey(ids, cacheBaseKey, keyRouteType);
        return baseCaffeineCacher.dangerously_PutAlreadyCachedFlux(finalKey, cachedFlux);
    }

    protected <T> Mono<T> saveToCache(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType, Mono<T> cachedMono) {
        K finalKey = createKey(ids, cacheBaseKey, keyRouteType);
        return baseCaffeineCacher.dangerously_PutAlreadyCachedMono(finalKey, cachedMono);
    }


    public FilteredListCaffeineCacheImpl(String cacheBaseKey) {
        this.baseCaffeineCacher = BaseCaffeienCacherImpl.GetBaseCaffeineCacherWithNoExpirationTime();
        this.cacheBaseKey = cacheBaseKey;
    }

    public FilteredListCaffeineCacheImpl(String cacheBaseKey, Integer cacheExpirationTimeMinutes, Integer cacheMaximumSize) {
        this.baseCaffeineCacher = new BaseCaffeienCacherImpl<>(cacheExpirationTimeMinutes, cacheMaximumSize);
        this.cacheBaseKey = cacheBaseKey;
    }

    protected String generateKeyForAttributes(List<Object> attributes, FilterKeyType.KeyRouteType keyRouteType) {
        return createKey(cacheBaseKey + attributes.stream()
                .filter(Objects::nonNull)
                .map(Objects::toString).toList(), keyRouteType).toString();
    }

    protected <T> Flux<T> getFluxCache(List<Object> attributes, FilterKeyType.KeyRouteType keyRouteType, Flux<T> flux, Function<T, Long> putIdCall) {
        String cacheKey = generateKeyForAttributes(attributes, keyRouteType);
        K filterKey = createKey(cacheKey, keyRouteType);

        return baseCaffeineCacher.<T>getCachedFluxOrEmpty(filterKey)
                .switchIfEmpty(
                        Flux.defer(() -> {
                            log.error("Cache miss Flux for key: {}", cacheKey);
                            Flux<T> cachedFlux = baseCaffeineCacher.cacheFlux(flux);
                            return cachedFlux
                                    .map(putIdCall)
                                    .collectList()
                                    .flatMapMany(ids -> {
                                        if (ids.isEmpty()) {
                                            return cachedFlux;
                                        }
                                        return saveToCache(ids, cacheKey, keyRouteType, cachedFlux);
                                    });
                        })
                );
    }

    protected <T> Mono<T> getMonoCache(List<Object> attributes, FilterKeyType.KeyRouteType keyRouteType, Mono<T> mono, Function<T, Long> putIdCall) {
        String cacheKey = generateKeyForAttributes(attributes, keyRouteType);
        K filterKey = createKey(cacheKey, keyRouteType);

        return baseCaffeineCacher.<T>getCachedMonoOrEmpty(filterKey)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            log.error("Cache miss Mono for key: {}", cacheKey);
                            Mono<T> cachedMono = baseCaffeineCacher.cacheMono(mono);
                            return cachedMono
                                    .switchIfEmpty(cachedMono)
                                    .flatMap(r -> {
                                        List<Long> id = new ArrayList<>();
                                        id.add(putIdCall.apply(r));
                                        return saveToCache(id, cacheKey, keyRouteType, cachedMono);
                                    });
                        })
                );
    }

    protected <T> Mono<T> getMonoCacheIdList(List<Object> attributes, FilterKeyType.KeyRouteType keyRouteType, Mono<T> mono, List<Long> ids) {
        String cacheKey = generateKeyForAttributes(attributes, keyRouteType);
        K filterKey = createKey(cacheKey, keyRouteType);

        return baseCaffeineCacher.<T>getCachedMonoOrEmpty(filterKey)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            log.error("Cache miss Mono id list for key: {}", cacheKey);
                            Mono<T> cachedMono = baseCaffeineCacher.cacheMono(mono);
                            return cachedMono
                                    .switchIfEmpty(cachedMono)
                                    .flatMap(r -> saveToCache(ids, cacheKey, keyRouteType, cachedMono));
                        })
                );
    }

    @Override
    public <T> Mono<T> getUniqueMonoCacheIdList(List<Object> attributes, String uniqueGiver, List<Long> ids, FilterKeyType.KeyRouteType keyRouteType, Mono<T> mono) {
        List<Object> attributesUnique = new ArrayList<>(attributes);
        attributesUnique.add(uniqueGiver);
        return getMonoCacheIdList(attributesUnique, keyRouteType, mono, ids);
    }

    @Override
    public <T> Mono<T> getUniqueMonoCache(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, FilterKeyType.KeyRouteType keyRouteType, Mono<T> mono) {
        List<Object> attributesUnique = new ArrayList<>(attributes);
        attributesUnique.add(uniqueGiver);
        return getMonoCache(attributesUnique, keyRouteType, mono, putIdCall);
    }

    @Override
    public <T> Mono<T> getUniqueMonoCacheIndependent(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, Mono<T> mono) {
        return getUniqueMonoCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createIndependent(), mono);
    }

    @Override
    public <T> Mono<T> getUniqueMonoCacheIdListIndependent(List<Object> attributes, String uniqueGiver, List<Long> ids, Mono<T> mono) {
        return getUniqueMonoCacheIdList(attributes, uniqueGiver, ids, FilterKeyType.KeyRouteType.createIndependent(), mono);
    }

    @Override
    public <T> Flux<T> getUniqueFluxCache(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, FilterKeyType.KeyRouteType keyRouteType, Flux<T> flux) {
        List<Object> attributesUnique = new ArrayList<>(attributes);
        attributesUnique.add(uniqueGiver);
        return getFluxCache(attributesUnique, keyRouteType, flux, putIdCall);
    }

    @Override
    public <T> Flux<T> getUniqueFluxCacheIndependent(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, Flux<T> flux) {
        return getUniqueFluxCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createIndependent(), flux);
    }


    @Override
    public <T> Flux<T> getUniqueFluxCacheForTrainer(List<Object> attributes, Long trainerId, String uniqueGiver, Function<T, Long> putIdCall, Flux<T> flux) {
        return getUniqueFluxCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createForTrainer(trainerId), flux);
    }

    @Override
    public <T> Flux<T> getUniqueFluxCacheForAdmin(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, Flux<T> flux) {
        return getUniqueFluxCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createForAdmin(), flux);
    }

    @Override
    public <T> Flux<T> getUniqueFluxCacheForPublic(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, Flux<T> flux) {
        return getUniqueFluxCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createForPublic(), flux);
    }

    @Override
    public Predicate<K> idContainingPredicate(Long id) {
        log.error("idContainingPredicate: {}", id);
        return key -> key.getIds() == null || key.getIds().contains(id);
    }

    @Override
    public Predicate<K> idContainingPredicate(List<Long> ids) {
        log.error("idContainingPredicate: {}", ids);
        return key -> key.getIds() == null || key.getIds().stream().anyMatch(ids::contains);
    }

    @Override
    public Predicate<K> deleteAllPredicate() {
        return key -> true;
    }

    @Override
    public Predicate<K> byTrainerIdPredicate(Long trainerId) {
        log.error("byTrainerIdPredicate: {}", trainerId);
        return key -> key.getRouteType().getTrainerId().equals(trainerId);
    }

    @Override
    public Predicate<K> byAdminPredicate() {
        log.error("byAdminPredicate");
        return key -> key.getRouteType().getIsAdmin();
    }

    @Override
    public Predicate<K> byPublicPredicate() {
        log.error("byPublicPredicate");
        return key -> key.getRouteType().getIsPublic();
    }

    @Override
    public Predicate<K> byIdAndTrainerIdPredicate(Long id, Long trainerId) {
        return key -> idContainingPredicate(id).and(byTrainerIdPredicate(trainerId)).test(key);
    }

    @Override
    public Predicate<K> byIdAndAdminPredicate(Long id) {
        return key -> idContainingPredicate(id).and(byAdminPredicate()).test(key);
    }

    @Override
    public Predicate<K> byIdAndPublicPredicate(Long id) {
        return key -> idContainingPredicate(id).and(byPublicPredicate()).test(key);
    }


    @Override
    public Mono<Void> invalidateByVoid(Predicate<K> predicate) {
        return baseCaffeineCacher.invalidateAllCache(
                baseCaffeineCacher.getKeysByCriteria(predicate)
        );
    }

    @SafeVarargs
    @Override
    public final Predicate<K> combinePredicatesAnd(Predicate<K>... predicates) {
        Predicate<K> combinedPredicate = key -> true;
        for (Predicate<K> predicate : predicates) {
            combinedPredicate = combinedPredicate.and(predicate);
        }
        return combinedPredicate;
    }

    @SafeVarargs
    @Override
    public final Predicate<K> combinePredicatesOr(Predicate<K>... predicates) {
        Predicate<K> combinedPredicate = key -> false;
        for (Predicate<K> predicate : predicates) {
            combinedPredicate = combinedPredicate.or(predicate);
        }
        return combinedPredicate;
    }

    public <T> Mono<Void> invalidateBy(Predicate<K> predicate) {
        return baseCaffeineCacher.invalidateAllCache(
                baseCaffeineCacher.getKeysByCriteria(predicate)
        );
    }

    @Override
    public <T> Flux<T> invalidateByWrapper(Flux<T> flux, Predicate<K> predicate) {
        return baseCaffeineCacher.invalidateAllCache(
                baseCaffeineCacher.getKeysByCriteria(predicate)
        ).thenMany(flux);
    }

    @Override
    public <T> Mono<T> invalidateByWrapper(Mono<T> mono, Predicate<K> predicate) {
        return baseCaffeineCacher.invalidateAllCache(
                baseCaffeineCacher.getKeysByCriteria(predicate)
        ).then(mono);
    }


    @Override
    public Mono<Void> invalidateAllCache() {
        log.error("Invalidating all cache");
        return baseCaffeineCacher.invalidateAllCache();
    }


    @Override
    public <T> Flux<T> invalidateAllCache(Flux<T> flux) {
        return invalidateAllCache()
                .thenMany(flux);
    }


    @Override
    public <T> Mono<T> invalidateAllCache(Mono<T> mono) {
        return invalidateAllCache()
                .then(mono);
    }

    @Override
    public <T> Mono<T> invalidateByIdFlatten(T t, Long id) {
        return invalidateByWrapper(Mono.just(t), idContainingPredicate(id));
    }


//    public <T> Mono<T> invalidateById(Long id, Mono<T> mono) {
//        return invalidateByWrapper(mono, idContainingPredicate(id));
//    }

    @Override
    public Mono<CustomEntityModel<R>> invalidateByWrapperEntity(Mono<CustomEntityModel<R>> mono, Function<R, Predicate<K>> predicate) {
        return mono.flatMap(cm -> invalidateBy(predicate.apply(cm.getContent())).thenReturn(cm));
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<R>> invalidateByWrapperEntityUser(Mono<ResponseWithUserDtoEntity<R>> mono, Function<R, Predicate<K>> predicate) {
        return mono.flatMap(cm -> invalidateBy(predicate.apply(cm.getModel().getContent())).thenReturn(cm));
    }

    @Override
    public <T> Mono<T> invalidateByWrapperCallback(Mono<T> t, Function<T, Predicate<K>> predicate) {
        return t.flatMap(tf -> invalidateBy(predicate.apply(tf)).thenReturn(tf));
    }

    @Override
    public BaseCaffeineCacher<K> getBaseCaffeineCacher() {
        return baseCaffeineCacher;
    }

    @Override
    public Predicate<K> byIdsList(List<Long> ids) {

        Predicate<K> predicate = key -> false;
        for (Long id : ids) {
            predicate = predicate.or(idContainingPredicate(id));
        }

        return predicate;

    }

}


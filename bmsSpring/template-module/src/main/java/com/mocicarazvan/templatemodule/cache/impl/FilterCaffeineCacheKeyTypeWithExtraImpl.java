package com.mocicarazvan.templatemodule.cache.impl;

import com.mocicarazvan.templatemodule.cache.FilterCaffeineCacheKeyTypeWithExtra;
import com.mocicarazvan.templatemodule.cache.keys.FilerKeyTypeWithExtra;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDtoEntity;
import com.mocicarazvan.templatemodule.hateos.CustomEntityModel;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.function.Function5;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public abstract class FilterCaffeineCacheKeyTypeWithExtraImpl<E, M, K extends FilerKeyTypeWithExtra<E, M>, R>
        extends FilteredListCaffeineCacheImpl<K, R>
        implements FilterCaffeineCacheKeyTypeWithExtra<K, M, R> {
    protected final M defaultMap;
    protected final E independentExtraOfMap;
    private final E invalidateOnCreateExtra;

    public FilterCaffeineCacheKeyTypeWithExtraImpl(String cacheBaseKey, M defaultMap, E independentExtraOfMap, E invalidateOnCreateExtra) {
        super(cacheBaseKey);
        this.defaultMap = defaultMap;
        this.independentExtraOfMap = independentExtraOfMap;
        this.invalidateOnCreateExtra = invalidateOnCreateExtra;
    }

    public FilterCaffeineCacheKeyTypeWithExtraImpl(String cacheBaseKey, Integer cacheExpirationTimeMinutes, Integer cacheMaximumSize, M defaultMap, E independentExtraOfMap, E invalidateOnCreateExtra) {
        super(cacheBaseKey, cacheExpirationTimeMinutes, cacheMaximumSize);
        this.defaultMap = defaultMap;
        this.independentExtraOfMap = independentExtraOfMap;
        this.invalidateOnCreateExtra = invalidateOnCreateExtra;
    }

    @Override
    public M getDefaultMap() {
        return defaultMap;
    }

    @Override
    public E getIndependentExtraOfMap() {
        return independentExtraOfMap;
    }


    protected abstract K createKey(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType, M extra);

    protected abstract K createKeyIndependentOfExtra(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType);

    protected <T> Flux<T> saveExtraToCache(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType, M extra, Flux<T> cachedFlux) {
        K finalKey = createKey(ids, cacheBaseKey, keyRouteType, extra);
        return baseCaffeineCacher.dangerously_PutAlreadyCachedFlux(finalKey, cachedFlux);
    }

    protected <T> Flux<T> saveExtraToCacheIndependent(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType, Flux<T> cachedFlux) {
        K finalKey = createKeyIndependentOfExtra(ids, cacheBaseKey, keyRouteType);
        return baseCaffeineCacher.dangerously_PutAlreadyCachedFlux(finalKey, cachedFlux);
    }

    protected <T> Mono<T> saveExtraToCache(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType, M extra, Mono<T> cachedMono) {
        K finalKey = createKey(ids, cacheBaseKey, keyRouteType, extra);
        return baseCaffeineCacher.dangerously_PutAlreadyCachedMono(finalKey, cachedMono);
    }

    protected <T> Mono<T> saveExtraToCacheIndependent(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType, Mono<T> cachedMono) {
        K finalKey = createKeyIndependentOfExtra(ids, cacheBaseKey, keyRouteType);
        return baseCaffeineCacher.dangerously_PutAlreadyCachedMono(finalKey, cachedMono);
    }

    protected <T> Flux<T> getExtraFluxCache(List<Object> attributes, FilterKeyType.KeyRouteType keyRouteType, M extra, Flux<T> flux, Function<T, Long> putIdCall
            , Function5<List<Long>, String, FilterKeyType.KeyRouteType, M, Flux<T>, Flux<T>> saveCall

    ) {
        String cacheKey = generateKeyForAttributes(attributes, keyRouteType);
        K filterKey = createKey(cacheKey, keyRouteType);

        return baseCaffeineCacher.<T>getCachedFluxOrEmpty(filterKey)
                .switchIfEmpty(
                        Flux.defer(() -> {
                            log.error("Cache miss for key: {}", cacheKey);
                            Flux<T> cachedFlux = baseCaffeineCacher.cacheFlux(flux);
                            return cachedFlux
                                    .map(putIdCall)
                                    .collectList()
                                    .flatMapMany(ids -> {

                                        if (ids.isEmpty()) {
                                            return cachedFlux;
                                        }
//                                        return saveExtraToCache(ids, cacheKey, keyRouteType, extra, cachedFlux);
                                        return saveCall.apply(ids, cacheKey, keyRouteType, extra, cachedFlux);
                                    });
                        })
                );

    }

    protected <T> Mono<T> getExtraMonoCache(List<Object> attributes, FilterKeyType.KeyRouteType keyRouteType, M extra, Mono<T> mono, Function<T, Long> putIdCall
            , Function5<List<Long>, String, FilterKeyType.KeyRouteType, M, Mono<T>, Mono<T>> saveCall

    ) {
        String cacheKey = generateKeyForAttributes(attributes, keyRouteType);
        K filterKey = createKey(cacheKey, keyRouteType);

        return baseCaffeineCacher.<T>getCachedMonoOrEmpty(filterKey)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            log.error("Cache miss for key: {}", cacheKey);
                            Mono<T> cachedMono = baseCaffeineCacher.cacheMono(mono);
                            return cachedMono
                                    .switchIfEmpty(cachedMono)
                                    .flatMap(r -> {
                                        List<Long> id = new ArrayList<>();
                                        id.add(putIdCall.apply(r));
//                                        return saveExtraToCache(id, cacheKey, keyRouteType, extra, cachedMono);
                                        return saveCall.apply(id, cacheKey, keyRouteType, extra, cachedMono);
                                    });
                        })
                );
    }

    protected <T> Mono<T> getExtraMonoCacheIdList(List<Object> attributes, FilterKeyType.KeyRouteType keyRouteType, M extra, Mono<T> mono, List<Long> ids
            , Function5<List<Long>, String, FilterKeyType.KeyRouteType, M, Mono<T>, Mono<T>> saveCall

    ) {
        String cacheKey = generateKeyForAttributes(attributes, keyRouteType);
        K filterKey = createKey(cacheKey, keyRouteType);

        return baseCaffeineCacher.<T>getCachedMonoOrEmpty(filterKey)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            log.error("Cache miss for key: {}", cacheKey);
                            Mono<T> cachedMono = baseCaffeineCacher.cacheMono(mono);
                            return cachedMono
                                    .switchIfEmpty(cachedMono)
                                    .flatMap(r -> {
//                                        return saveExtraToCache(id, cacheKey, keyRouteType, extra, cachedMono);
                                        return saveCall.apply(ids, cacheKey, keyRouteType, extra, cachedMono);
                                    });
                        })
                );
    }

    @Override
    public <T> Mono<T> getExtraUniqueMonoCacheIdList(List<Object> attributes, String uniqueGiver,
                                                     List<Long> ids,
                                                     FilterKeyType.KeyRouteType keyRouteType,
                                                     M extra, Mono<T> mono) {
        List<Object> attributesUnique = new ArrayList<>(attributes);
        attributesUnique.add(uniqueGiver);
        return getExtraMonoCacheIdList(attributesUnique, keyRouteType, extra, mono, ids, this::saveExtraToCache);
    }

    @Override
    public <T> Mono<T> getExtraUniqueMonoCache(List<Object> attributes, String uniqueGiver,
                                               Function<T, Long> putIdCall,
                                               FilterKeyType.KeyRouteType keyRouteType,
                                               M extra, Mono<T> mono) {
        List<Object> attributesUnique = new ArrayList<>(attributes);
        attributesUnique.add(uniqueGiver);
        return getExtraMonoCache(attributesUnique, keyRouteType, extra, mono, putIdCall, this::saveExtraToCache);
    }


    @Override
    public <T> Mono<T> getExtraUniqueMonoCacheIndependent(List<Object> attributes, String uniqueGiver,
                                                          Function<T, Long> putIdCall,
                                                          Mono<T> mono) {
        List<Object> attributesUnique = new ArrayList<>(attributes);
        attributesUnique.add(uniqueGiver);
        return getExtraMonoCache(attributesUnique, FilterKeyType.KeyRouteType.createIndependent(), defaultMap, mono, putIdCall
                , (ids, cacheKey, keyRouteType, _extra, cachedMono) -> saveExtraToCacheIndependent(ids, cacheKey, keyRouteType, cachedMono)
        );

    }

    @Override
    public <T> Mono<T> getExtraUniqueMonoCacheForPublic(List<Object> attributes, String uniqueGiver,
                                                        Function<T, Long> putIdCall,
                                                        M extra, Mono<T> mono) {
        return getExtraUniqueMonoCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createForPublic(), extra, mono);
    }

    @Override
    public <T> Mono<T> getExtraUniqueMonoCacheForAdmin(List<Object> attributes, String uniqueGiver,
                                                       Function<T, Long> putIdCall,
                                                       M extra, Mono<T> mono) {
        return getExtraUniqueMonoCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createForAdmin(), extra, mono);
    }

    @Override
    public <T> Mono<T> getExtraUniqueMonoCacheForTrainer(List<Object> attributes, Long trainerId, String uniqueGiver,
                                                         Function<T, Long> putIdCall,
                                                         M extra, Mono<T> mono) {
        return getExtraUniqueMonoCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createForTrainer(trainerId), extra, mono);
    }

    @Override
    public <T> Mono<T> getExtraUniqueMonoCacheIdListIndependent(List<Object> attributes, String uniqueGiver,
                                                                List<Long> ids,
                                                                Mono<T> mono) {
        List<Object> attributesUnique = new ArrayList<>(attributes);
        attributesUnique.add(uniqueGiver);
        return getExtraMonoCacheIdList(attributesUnique, FilterKeyType.KeyRouteType.createIndependent(), defaultMap, mono, ids
                , (i, cacheKey, keyRouteType, _extra, cachedMono) -> saveExtraToCacheIndependent(i, cacheKey, keyRouteType, cachedMono)
        );

    }


    @Override
    public <T> Flux<T> getExtraUniqueFluxCache(List<Object> attributes, String uniqueGiver,
                                               Function<T, Long> putIdCall,
                                               FilterKeyType.KeyRouteType keyRouteType,
                                               M extra, Flux<T> flux) {
        List<Object> attributesUnique = new ArrayList<>(attributes);
        attributesUnique.add(uniqueGiver);
        return getExtraFluxCache(attributesUnique, keyRouteType, extra, flux, putIdCall, this::saveExtraToCache);
    }

    @Override
    public <T> Flux<T> getExtraUniqueFluxCacheIndependent(List<Object> attributes, String uniqueGiver,
                                                          Function<T, Long> putIdCall, Flux<T> flux) {
        List<Object> attributesUnique = new ArrayList<>(attributes);
        attributesUnique.add(uniqueGiver);
        return getExtraFluxCache(attributesUnique, FilterKeyType.KeyRouteType.createIndependent(), defaultMap, flux, putIdCall,
                (ids, cacheKey, keyRouteType, _extra, cachedFlux) -> saveExtraToCacheIndependent(ids, cacheKey, keyRouteType, cachedFlux)
        );
    }

    @Override
    public <T> Flux<T> getExtraUniqueCacheForTrainer(List<Object> attributes, Long trainerId, String uniqueGiver, Function<T, Long> putIdCall, M extra, Flux<T> flux) {
        return getExtraUniqueFluxCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createForTrainer(trainerId), extra, flux);
    }

    @Override
    public <T> Flux<T> getExtraUniqueCacheForAdmin(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, M extra, Flux<T> flux) {
        return getExtraUniqueFluxCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createForAdmin(), extra, flux);
    }

    @Override
    public <T> Flux<T> getExtraUniqueCacheForPublic(List<Object> attributes, String uniqueGiver, Function<T, Long> putIdCall, M extra, Flux<T> flux) {
        return getExtraUniqueFluxCache(attributes, uniqueGiver, putIdCall, FilterKeyType.KeyRouteType.createForPublic(), extra, flux);
    }

    @Override
    public Predicate<K> extraContainingPredicate(M extra) {
        log.error("extraContainingPredicate: {}", extra);
        return key -> key.getExtra() == null || key.getExtra().equals(extra);
    }


    @Override
    public Mono<CustomEntityModel<R>> invalidateByWrapperEntityExtra(Mono<Pair<CustomEntityModel<R>, M>> mono, Function<Pair<R, M>, Predicate<K>> predicate) {
        return mono.flatMap(pair -> invalidateBy(predicate.apply(
                Pair.of(pair.getFirst().getContent(), pair.getSecond()))
        ).thenReturn(pair.getFirst()));
    }

    @Override
    public Mono<ResponseWithUserDtoEntity<R>> invalidateByWrapperEntityUserExtra(Mono<ResponseWithUserDtoEntity<R>> mono, Function<R, Predicate<K>> predicate) {
        return mono.flatMap(responseWithUserDtoEntity -> invalidateBy(predicate.apply(responseWithUserDtoEntity.getModel().getContent())).thenReturn(responseWithUserDtoEntity));
    }

    @Override
    public Predicate<K> byInvalidateOnCreatePredicate() {
        return key -> key.getExtra() == null || key.getExtra().equals(invalidateOnCreateExtra);
    }


}

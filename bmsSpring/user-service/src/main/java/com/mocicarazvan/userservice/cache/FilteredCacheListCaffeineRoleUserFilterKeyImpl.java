package com.mocicarazvan.userservice.cache;

import com.mocicarazvan.templatemodule.cache.impl.FilterCaffeineCacheKeyTypeWithExtraImpl;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.utils.EntitiesUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Predicate;

@Service
public class FilteredCacheListCaffeineRoleUserFilterKeyImpl
        extends FilterCaffeineCacheKeyTypeWithExtraImpl<Set<String>, Set<Role>, RoleUserFilterKey, UserDto>
        implements FilteredCacheListCaffeineRoleUserFilterKey {


    private static final HashSet<String> independentExtraOfMap = new HashSet<>(Collections.singleton("independentExtraOfMap"));
    private static final HashSet<String> invalidateOnCreateExtra = new HashSet<>(Collections.singleton("invalidateOnCreateExtra"));
    private static final String cacheBaseKey = "user";

    public FilteredCacheListCaffeineRoleUserFilterKeyImpl() {
        super(cacheBaseKey, new HashSet<>(Collections.emptySet()), independentExtraOfMap, invalidateOnCreateExtra);
    }

    public FilteredCacheListCaffeineRoleUserFilterKeyImpl(Integer cacheExpirationTimeMinutes, Integer cacheMaximumSize) {
        super(cacheBaseKey, cacheExpirationTimeMinutes, cacheMaximumSize, new HashSet<>(Collections.emptySet()), independentExtraOfMap, invalidateOnCreateExtra);
    }


    @Override
    protected RoleUserFilterKey createKey(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType, Set<Role> extra) {
        return new RoleUserFilterKey(ids, cacheBaseKey, keyRouteType, extra);
    }

    @Override
    protected RoleUserFilterKey createKeyIndependentOfExtra(List<Long> ids, String cacheBaseKey, FilterKeyType.KeyRouteType keyRouteType) {
        RoleUserFilterKey key = new RoleUserFilterKey();
        key.setIds(ids);
        key.setKey(cacheBaseKey);
        key.setRouteType(keyRouteType);
        key.setActualExtra(independentExtraOfMap);
        return key;
    }

    @Override
    protected RoleUserFilterKey createKey(String key, FilterKeyType.KeyRouteType keyRouteType) {
        return new RoleUserFilterKey(key, keyRouteType, defaultMap);

    }

    @Override
    protected RoleUserFilterKey createKey(List<Long> ids, String key, FilterKeyType.KeyRouteType keyRouteType) {
        return new RoleUserFilterKey(ids, key, keyRouteType, defaultMap);
    }

    @Override
    public Predicate<FilterKeyType> updateDeleteBasePredicate(Long id, Long userId) {
        return k -> true;
    }

    @Override
    public Predicate<FilterKeyType> createBasePredicate(Long userId) {
        return k -> true;
    }

    @Override
    public Predicate<RoleUserFilterKey> byRolePredicate(Role role) {
        return key -> key.getExtra() == null || key.getExtra().isEmpty() || key.getExtra().contains(key.mapToExtraSingle(role));
    }


    @Override
    public Predicate<RoleUserFilterKey> roleCreatePredicate() {
        return byRolePredicate(Role.ROLE_USER);
    }

    @Override
    public Predicate<RoleUserFilterKey> roleMakeTrainerPredicate() {
        return key -> key.getExtra() == null || key.getExtra().isEmpty() || key.getExtra().contains(key.mapToExtraSingle(Role.ROLE_TRAINER))
                || key.getExtra().contains(key.mapToExtraSingle(Role.ROLE_USER));
    }

    @Override
    public Predicate<RoleUserFilterKey> createUserPredicateFinal() {
        return combinePredicatesOr(
                combinePredicatesAnd(
                        roleCreatePredicate(),
                        byAdminPredicate()
                ),
                combinePredicatesAnd(
                        roleCreatePredicate(),
                        byPublicPredicate()
                )
        );
    }


    @Override
    public Predicate<RoleUserFilterKey> makeTrainerPredicateFinal(Long id) {
        return combinePredicatesOr(
                combinePredicatesAnd(
                        roleMakeTrainerPredicate(),
                        byAdminPredicate()
                ),
                combinePredicatesAnd(
                        roleMakeTrainerPredicate(),
                        byPublicPredicate()
                ),
                idContainingPredicate(id)
        );
    }

    @Override
    public Predicate<RoleUserFilterKey> updateUserPredicateFinal(Long id, Role originalRole) {
        return combinePredicatesOr(
                combinePredicatesAnd(
                        byAdminPredicate(),
                        byRolePredicate(originalRole)
                ),
                combinePredicatesAnd(
                        byPublicPredicate(),
                        byRolePredicate(originalRole)
                )
                , idContainingPredicate(id)
        );
    }

    @Override
    public <T> Mono<T> createUserInvalidate(T t) {
        return invalidateByWrapper(Mono.just(t), createUserPredicateFinal());
    }


    @Override
    public Flux<PageableResponse<UserDto>> getAllUsersPersist(
            Flux<PageableResponse<UserDto>> flux,
            PageableBody pageableBody, String email, Set<Role> roles,
            Set<AuthProvider> providers, Boolean emailVerified, Boolean admin) {
        FilterKeyType.KeyRouteType keyRouteType = Boolean.TRUE.equals(admin) ? FilterKeyType.KeyRouteType.createForAdmin() : FilterKeyType.KeyRouteType.createForPublic();
        return getExtraUniqueFluxCache(
                EntitiesUtils.getListOfNotNullObjects(pageableBody, email, roles, providers, emailVerified, admin),
                "getAllUsers",
                m -> m.getContent().getId(),
                keyRouteType,
                roles,
                flux
        );
    }

    @Override
    public Mono<UserDto> makeTrainerInvalidate(Mono<UserDto> mono, Long id) {
        return invalidateByWrapper(mono, makeTrainerPredicateFinal(id));
    }

    @Override
    public Mono<UserDto> updateUserInvalidate(Mono<UserDto> mono, Long id) {
        return mono.flatMap(userDto -> invalidateByWrapper(Mono.just(userDto), updateUserPredicateFinal(id, userDto.getRole())));
    }

    @Override
    public Mono<UserDto> getUserPersist(Mono<UserDto> mono, Long id) {
        return getExtraUniqueMonoCacheIndependent(
                EntitiesUtils.getListOfNotNullObjects(id),
                "getUser" + id,
                IdGenerateDto::getId,
                mono
        );
    }

    @Override
    public Mono<Boolean> existsUserByIdAndRoleInPersist(Mono<Boolean> mono, Long userId, Set<Role> roles) {
        return getExtraUniqueMonoCacheForPublic(
                EntitiesUtils.getListOfNotNullObjects(userId, roles),
                "existsUserByIdAndRoleInPersist" + userId + roles,
                i -> userId,
                roles,
                mono
        );
    }

    @Override
    public Flux<UserDto> getUsersByIdInPersist(Flux<UserDto> flux, List<Long> ids) {
        return getExtraUniqueFluxCacheIndependent(
                EntitiesUtils.getListOfNotNullObjects(ids),
                "getUsersByIdInPersist" + ids,
                IdGenerateDto::getId,
                flux
        );
    }

}

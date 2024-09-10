package com.mocicarazvan.userservice.cache;

import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface FilteredCacheListCaffeineRoleUserFilterKey {
    Predicate<RoleUserFilterKey> byRolePredicate(Role role);

    Predicate<RoleUserFilterKey> roleCreatePredicate();

    Predicate<RoleUserFilterKey> roleMakeTrainerPredicate();

    Predicate<RoleUserFilterKey> createUserPredicateFinal();

    Predicate<RoleUserFilterKey> makeTrainerPredicateFinal(Long id);

    Predicate<RoleUserFilterKey> updateUserPredicateFinal(Long id, Role orginalRole);

    <T> Mono<T> createUserInvalidate(T t);

    Flux<PageableResponse<UserDto>> getAllUsersPersist(
            Flux<PageableResponse<UserDto>> flux,
            PageableBody pageableBody, String email, Set<Role> roles,
            Set<AuthProvider> providers, Boolean emailVerified, Boolean admin);

    Mono<UserDto> makeTrainerInvalidate(Mono<UserDto> mono, Long id);

    Mono<UserDto> updateUserInvalidate(Mono<UserDto> mono, Long id);

    Mono<UserDto> getUserPersist(Mono<UserDto> mono, Long id);

    Mono<Boolean> existsUserByIdAndRoleInPersist(Mono<Boolean> mono, Long userId, Set<Role> roles);

    Flux<UserDto> getUsersByIdInPersist(Flux<UserDto> flux, List<Long> ids);
}

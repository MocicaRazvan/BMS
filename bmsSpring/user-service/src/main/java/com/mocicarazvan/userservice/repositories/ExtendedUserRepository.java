package com.mocicarazvan.userservice.repositories;

import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.userservice.models.UserCustom;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface ExtendedUserRepository {

    Flux<UserCustom> getUsersFiltered(PageRequest pageRequest, String email, Set<Role> roles, Set<AuthProvider> providers, Boolean emailVerified);

    Mono<Long> countUsersFiltered(String email, Set<Role> roles, Set<AuthProvider> providers, Boolean emailVerified);
}

package com.mocicarazvan.userservice.services;


import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.models.UserCustom;
import reactor.core.publisher.Mono;

public interface HandleUserProvider {

    Mono<AuthResponse> saveOrUpdateUserProvider(AuthProvider provider, UserCustom user);

    Mono<AuthResponse> generateResponse(UserCustom user, AuthProvider authProvider);
}

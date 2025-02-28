package com.mocicarazvan.userservice.services.impl;


import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.models.UserCustom;
import com.mocicarazvan.userservice.services.HandleUserProvider;
import com.mocicarazvan.userservice.services.OauthUserInfoHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class GoogleUserInfo implements OauthUserInfoHandler {
    private final WebClient.Builder webClient;
    private final HandleUserProvider handleUserProvider;

    @Override
    public Mono<AuthResponse> handleUserInfo(OAuth2AccessTokenResponse accessTokenResponse, AuthProvider provider, Map<String, Object> userInfo) {
        String lastName = getFromMap(userInfo, "family_name");
        String firstName = getFromMap(userInfo, "given_name");
        String picture = getFromMap(userInfo, "picture");
        String email = getFromMap(userInfo, "email");

        UserCustom user = UserCustom.builder()
                .lastName(lastName)
                .firstName(firstName)
                .role(Role.ROLE_USER)
                .provider(provider)
                .image(picture)
                .email(email)
                .emailVerified(true)
                .build();
        return handleUserProvider.saveOrUpdateUserProvider(provider, user);

    }
}

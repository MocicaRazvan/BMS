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
        String lastName = userInfo.getOrDefault("family_name", "") != null ? userInfo.get("family_name").toString() : "";
        String firstName = userInfo.getOrDefault("given_name", "") != null ? userInfo.get("given_name").toString() : "";
        String picture = userInfo.getOrDefault("picture", "") != null ? userInfo.get("picture").toString() : "";
        String email = userInfo.getOrDefault("email", "") != null ? userInfo.get("email").toString() : "";

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

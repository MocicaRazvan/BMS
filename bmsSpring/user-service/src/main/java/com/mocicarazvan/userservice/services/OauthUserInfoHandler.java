package com.mocicarazvan.userservice.services;


import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@FunctionalInterface
public interface OauthUserInfoHandler {

    Mono<AuthResponse> handleUserInfo(OAuth2AccessTokenResponse accessTokenResponse, AuthProvider provider, Map<String, Object> userInfo);


}

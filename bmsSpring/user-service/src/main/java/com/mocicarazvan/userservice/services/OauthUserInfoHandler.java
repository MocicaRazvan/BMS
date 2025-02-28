package com.mocicarazvan.userservice.services;


import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@FunctionalInterface
public interface OauthUserInfoHandler {

    Mono<AuthResponse> handleUserInfo(OAuth2AccessTokenResponse accessTokenResponse, AuthProvider provider, Map<String, Object> userInfo);

    default String getFromMap(Map<String, Object> map, String key) {
        return (map.containsKey(key) && map.get(key) != null) ? map.get(key).toString() : "";
    }
}

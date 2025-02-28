package com.mocicarazvan.userservice.services.impl;

import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.userservice.dtos.auth.requests.CallbackBody;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.services.HandleUserProvider;
import com.mocicarazvan.userservice.services.OauthService;
import com.mocicarazvan.userservice.services.OauthUserInfoFactory;
import com.mocicarazvan.userservice.services.OauthUserInfoHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;


@RequiredArgsConstructor
@Slf4j
public class OauthServiceImpl implements OauthService {

    protected final String clientId;
    protected final String clientSecret;
    protected final String tokenUri;
    protected final String userInfoUri;
    protected final String redirectUri;
    protected final AuthProvider provider;
    protected final WebClient.Builder webClient;
    protected final OauthUserInfoFactory oauthUserInfoFactory;
    protected final HandleUserProvider handleUserProvider;
    protected final Function<String, Map<String, String>> parseBody;


    @Override
    public Mono<OAuth2AccessTokenResponse> exchangeToken(String code, Function<String, Map<String, String>> parseBody, String codeVerifier) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(OAuth2ParameterNames.CLIENT_ID, clientId);
        formData.add(OAuth2ParameterNames.CLIENT_SECRET, clientSecret);
        formData.add(OAuth2ParameterNames.CODE, code);
        formData.add(OAuth2ParameterNames.GRANT_TYPE, "authorization_code");
        formData.add(OAuth2ParameterNames.REDIRECT_URI, redirectUri);
        formData.add("access_type", "offline");
        if (codeVerifier != null) {
            formData.add("code_verifier", codeVerifier);
        }

        return webClient.build().post()
                .uri(tokenUri)
                .header("Accept", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
//                    log.error("Client error during token exchange: {}", clientResponse);
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
//                                log.error("Error body: {}", errorBody);
                                return Mono.error(new RuntimeException("4xx error during token exchange: " + errorBody));
                            });

                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
//                    log.error("Server error during token exchange: {}", clientResponse);
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
//                                log.error("Error body: {}", errorBody);
                                return Mono.error(new RuntimeException("5xx error during token exchange: " + errorBody));
                            });
                })
                .bodyToMono(String.class)
                .flatMap(body -> {
                    Map<String, String> params = parseBody.apply(body);
                    OAuth2AccessTokenResponse accessTokenResponse = OAuth2AccessTokenResponse.withToken(params.get("access_token"))
                            .tokenType(OAuth2AccessToken.TokenType.BEARER)
                            .expiresIn(Long.parseLong(params.getOrDefault("expires_in", "36000")))
                            .scopes(Collections.singleton("read:user"))
                            .build();
                    return Mono.just(accessTokenResponse);
                })
                .onErrorResume(e -> {
                    log.error("Error while exchanging token: {}", e.getMessage());
                    return Mono.error(e);
                });
    }


    @Override
    public Mono<AuthResponse> createOauth2JwtToken(OAuth2AccessTokenResponse accessTokenResponse) {
        OauthUserInfoHandler oauthUserInfoHandler = oauthUserInfoFactory.getOauthUserInfoHandler(provider, handleUserProvider);
        return webClient.build().get()
                .uri(userInfoUri)
                .header("Authorization", "Bearer " + accessTokenResponse.getAccessToken().getTokenValue())
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .flatMap(userInfo -> oauthUserInfoHandler.handleUserInfo(accessTokenResponse, provider, userInfo)).onErrorResume(e -> {
//                    log.error("Error while creating oauth2 jwt token");
//                    log.error(e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<AuthResponse> handleProviderCallback(CallbackBody callbackBody, String codeVerifier) {
        return exchangeToken(callbackBody.getCode(), parseBody, codeVerifier)
                .flatMap(this::createOauth2JwtToken);
    }
}

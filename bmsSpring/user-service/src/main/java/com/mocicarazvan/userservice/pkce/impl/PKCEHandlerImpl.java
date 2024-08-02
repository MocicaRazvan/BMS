package com.mocicarazvan.userservice.pkce.impl;

import com.mocicarazvan.userservice.dtos.auth.requests.CallbackBody;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.exceptions.StateNotFound;
import com.mocicarazvan.userservice.models.OauthState;
import com.mocicarazvan.userservice.pkce.CodeVerifierResponse;
import com.mocicarazvan.userservice.pkce.PKCEHandler;
import com.mocicarazvan.userservice.pkce.PKCEService;
import com.mocicarazvan.userservice.pkce.PKCEUtil;
import com.mocicarazvan.userservice.repositories.OauthStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;


@RequiredArgsConstructor
@Slf4j
public abstract class PKCEHandlerImpl implements PKCEHandler {

    private final PKCEService pkceService;
    private final OauthStateRepository oauthStateRepository;
    private final String clientId;
    private final String redirectUri;
    private final String clientSecret;
    private final String authUri;

    public Mono<CodeVerifierResponse> handleAuthorizationCode(@RequestParam String state) {
        log.error("Initiating Google login");

        String codeVerifier = PKCEUtil.generateCodeVerifier();
        String codeChallenge = PKCEUtil.generateCodeChallenge(codeVerifier);
        log.error("State: {}", state);

        OauthState oauthState = new OauthState();
        oauthState.setState(state);
        oauthState.setCodeVerifier(codeVerifier);
        log.error("Generated code_verifier: {}", codeVerifier);

        return oauthStateRepository.save(oauthState)
                .map(o -> {
                    log.error("Saved state: {}", o);
                    return o;
                })
                .then(Mono.defer(() -> {
                    String authorizationUrl = pkceService.generateAuthorizationUrl(authUri,
                            clientId, redirectUri, state, codeChallenge);
                    log.error("Generated authorization URL: {}", authorizationUrl);

                    return Mono.just(CodeVerifierResponse.builder().url(authorizationUrl).build());
                }));
    }

    public Mono<AuthResponse> handleAuthResponse(
            CallbackBody callbackBody, BiFunction<CallbackBody, String, Mono<AuthResponse>> handleCallback
    ) {
        log.error("Received callback: {}", callbackBody);
        return oauthStateRepository.findByState(callbackBody.getState())
                .switchIfEmpty(Mono.error(new StateNotFound()))
                .flatMap(oauthState -> {
                    String codeVerifier = oauthState.getCodeVerifier();
                    log.error("Retrieved code_verifier from store: {}", codeVerifier);
                    return handleCallback.apply(callbackBody, codeVerifier)
                            .flatMap(resp -> oauthStateRepository.deleteByState(callbackBody.getState())
                                    .thenReturn(resp));
                });
    }

}

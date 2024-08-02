package com.mocicarazvan.userservice.pkce.impl;

import com.mocicarazvan.userservice.pkce.PKCEService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class PKCEServiceImpl implements PKCEService {

    public String generateAuthorizationUrl(String authUri, String clientId, String redirectUri, String state, String codeChallenge) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(authUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("access_type", "offline")
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("state", state);

        return builder.toUriString();
    }
}

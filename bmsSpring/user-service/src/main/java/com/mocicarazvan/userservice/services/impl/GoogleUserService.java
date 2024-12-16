package com.mocicarazvan.userservice.services.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.userservice.services.AbstractOauthProvider;
import com.mocicarazvan.userservice.services.OauthUserInfoFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j

public class GoogleUserService extends AbstractOauthProvider {

    public GoogleUserService(OauthUserInfoFactory oauthUserInfoFactory,
                             @Value("${google.client.id}") String clientId,
                             @Value("${google.client.secret}") String clientSecret,
                             @Value("${google.redirect.uri}") String redirectUri
    ) {
        super(oauthUserInfoFactory, clientId, clientSecret, redirectUri, "https://oauth2.googleapis.com/token",
                "https://www.googleapis.com/oauth2/v3/userinfo", AuthProvider.GOOGLE,
                body -> {
//                    log.error("GoogleUserService raw body " + body);
                    try {
                        return new ObjectMapper().readValue(body, new TypeReference<Map<String, String>>() {
                        });
                    } catch (Exception e) {
                        log.error("GoogleUserService error parsing body " + e.getMessage());
                        return new HashMap<>();
                    }
                });
    }
}

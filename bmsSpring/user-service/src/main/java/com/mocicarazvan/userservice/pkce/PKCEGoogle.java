package com.mocicarazvan.userservice.pkce;


import com.mocicarazvan.userservice.pkce.impl.PKCEHandlerImpl;
import com.mocicarazvan.userservice.repositories.OauthStateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class PKCEGoogle extends PKCEHandlerImpl {
    public PKCEGoogle(PKCEService pkceService, OauthStateRepository oauthStateRepository,
                      @Value("${google.client.id}") String clientId,
                      @Value("${google.redirect.uri}") String redirectUri,
                      @Value("${google.client.secret}") String clientSecret) {
        super(pkceService, oauthStateRepository, clientId, redirectUri, clientSecret, "https://accounts.google.com/o/oauth2/v2/auth");
    }


}

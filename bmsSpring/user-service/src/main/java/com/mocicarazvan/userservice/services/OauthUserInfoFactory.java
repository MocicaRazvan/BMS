package com.mocicarazvan.userservice.services;


import com.mocicarazvan.templatemodule.enums.AuthProvider;

@FunctionalInterface
public interface OauthUserInfoFactory {
    OauthUserInfoHandler getOauthUserInfoHandler(AuthProvider provider, HandleUserProvider handleUserProvider);

}

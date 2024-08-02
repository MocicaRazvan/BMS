package com.mocicarazvan.userservice.services.impl;

import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.jwt.JwtUtils;
import com.mocicarazvan.userservice.mappers.UserMapper;
import com.mocicarazvan.userservice.models.JwtToken;
import com.mocicarazvan.userservice.models.UserCustom;
import com.mocicarazvan.userservice.repositories.JwtTokenRepository;
import com.mocicarazvan.userservice.repositories.UserRepository;
import com.mocicarazvan.userservice.services.HandleUserProvider;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;


//@Service
@RequiredArgsConstructor
public class BasicUserProvider implements HandleUserProvider {

    protected final UserRepository userRepository;
    protected final JwtTokenRepository jwtTokenRepository;
    protected final JwtUtils jwtUtil;
    protected final UserMapper userMapper;

    public Mono<AuthResponse> saveOrUpdateUserProvider(AuthProvider provider, UserCustom user) {
        return userRepository.findByEmail(user.getEmail())
                .log()
                .flatMap(u -> generateResponse(u, u.getProvider()))
                .switchIfEmpty(userRepository.save(user)
                        .flatMap(u -> generateResponse(u, provider)));
    }

    public Mono<AuthResponse> generateResponse(UserCustom user, AuthProvider authProvider) {
        user.setProvider(authProvider);
        String token = jwtUtil.generateToken(user);
        JwtToken jwtToken = JwtToken.builder()
                .userId(user.getId())
                .token(token)
                .revoked(false)
                .build();

        return jwtTokenRepository.deleteAllByToken(token).then(
                jwtTokenRepository.save(jwtToken)
                        .map(t -> userMapper.fromUserCustomToAuthResponse(user).map(
                                u -> {
                                    u.setToken(jwtToken.getToken());
                                    return u;
                                }
                        )));

    }

}

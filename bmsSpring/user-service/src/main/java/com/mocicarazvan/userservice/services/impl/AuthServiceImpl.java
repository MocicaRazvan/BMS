package com.mocicarazvan.userservice.services.impl;

import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.exceptions.common.UsernameNotFoundException;
import com.mocicarazvan.userservice.cache.redis.annotations.RedisReactiveRoleCacheEvict;
import com.mocicarazvan.userservice.dtos.auth.requests.CallbackBody;
import com.mocicarazvan.userservice.dtos.auth.requests.LoginRequest;
import com.mocicarazvan.userservice.dtos.auth.requests.RegisterRequest;
import com.mocicarazvan.userservice.dtos.auth.requests.TokenValidationRequest;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.dtos.auth.response.TokenValidationResponse;
import com.mocicarazvan.userservice.exceptions.UserWithEmailExists;
import com.mocicarazvan.userservice.jwt.JwtUtils;
import com.mocicarazvan.userservice.mappers.UserMapper;
import com.mocicarazvan.userservice.models.UserCustom;
import com.mocicarazvan.userservice.repositories.JwtTokenRepository;
import com.mocicarazvan.userservice.repositories.UserRepository;
import com.mocicarazvan.userservice.services.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AuthServiceImpl extends BasicUserProvider implements AuthService {


    private final WebClient.Builder webClient;
    private final PasswordEncoder passwordEncoder;
    private final GithubUserService githubUserService;
    private final GoogleUserService googleUserService;
    private final UserEmbedServiceImpl userEmbedService;
    private final TransactionalOperator transactionalOperator;


    public AuthServiceImpl(UserRepository userRepository, JwtTokenRepository jwtTokenRepository, JwtUtils jwtUtil, UserMapper userMapper, WebClient.Builder webClient, PasswordEncoder passwordEncoder, GithubUserService githubUserService, GoogleUserService googleUserService, UserEmbedServiceImpl userEmbedService, TransactionalOperator transactionalOperator
    ) {
        super(userRepository, jwtTokenRepository, jwtUtil, userMapper, transactionalOperator, userEmbedService);
        this.webClient = webClient;
        this.passwordEncoder = passwordEncoder;
        this.githubUserService = githubUserService;
        this.googleUserService = googleUserService;
        this.userEmbedService = userEmbedService;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    @RedisReactiveRoleCacheEvict(key = "userService")
    public Mono<AuthResponse> register(RegisterRequest registerRequest) {
        log.error(userMapper.fromRegisterRequestToUserCustom(registerRequest).toString());
        return userRepository.existsByEmail(registerRequest.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserWithEmailExists(registerRequest.getEmail()));
                    }
                    return userRepository.save(userMapper.fromRegisterRequestToUserCustom(registerRequest))
                            .flatMap(u -> userEmbedService.saveEmbedding(u.getId(), u.getEmail()).thenReturn(u))
                            .as(transactionalOperator::transactional)
                            .flatMap(u -> generateResponse(u, AuthProvider.LOCAL));
                });
    }

    @Override
    public Mono<AuthResponse> login(LoginRequest loginRequest) {
        return userRepository.findByEmail(loginRequest.getEmail())
                .filter(u -> {
                    log.info("User Provider: {}", u.getProvider());
                    return u.getProvider().equals(AuthProvider.LOCAL);
                })
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with email: " + loginRequest.getEmail() + " not found")))
                .flatMap(u ->
                        revokeOldTokens(u).then(Mono.defer(() -> {
                            if (!passwordEncoder.matches(loginRequest.getPassword(), u.getPassword())) {
                                log.error("Password not match");
                                return Mono.error(new UsernameNotFoundException("User with email: " + loginRequest.getEmail() + " not found"));
                            }
                            return generateResponse(u, AuthProvider.LOCAL);
                        }))
                );
    }

    @Override
    public Mono<TokenValidationResponse> validateToken(TokenValidationRequest tokenValidationRequest) {

        return userRepository.findByEmail(jwtUtil.extractUsername(tokenValidationRequest.getToken()))
                .flatMap(u -> {
                    boolean minRoleMatch = isMinRoleMatch(tokenValidationRequest, u);
                    if (!minRoleMatch) {
                        return Mono.just(TokenValidationResponse.builder().valid(false).build());
                    }
                    return jwtUtil.isTokenValid(tokenValidationRequest.getToken(), u.getUsername())
                            .map(valid -> {
                                if (!valid) {
                                    return TokenValidationResponse.builder().valid(false).build();
                                }
                                return TokenValidationResponse.builder().valid(true).userId(u.getId()).build();

                            });
                })
                .defaultIfEmpty(TokenValidationResponse.builder().valid(false).build())
                .onErrorResume(e -> Mono.just(TokenValidationResponse.builder().valid(false).build()));
    }

    private boolean isMinRoleMatch(TokenValidationRequest tokenValidationRequest, UserCustom u) {
        boolean minRoleMatch;
        if (tokenValidationRequest.getMinRoleRequired().equals(Role.ROLE_USER)) {
            minRoleMatch = u.getRole().equals(Role.ROLE_USER) || u.getRole().equals(Role.ROLE_ADMIN) || u.getRole().equals(Role.ROLE_TRAINER);
        } else if (tokenValidationRequest.getMinRoleRequired().equals(Role.ROLE_TRAINER)) {
            minRoleMatch = u.getRole().equals(Role.ROLE_TRAINER) || u.getRole().equals(Role.ROLE_ADMIN);
        } else {
            minRoleMatch = u.getRole().equals(Role.ROLE_ADMIN);
        }
        return minRoleMatch;
    }


    private Mono<Void> revokeOldTokens(UserCustom user) {
        return jwtTokenRepository.findAllByUserId(user.getId())
                .flatMap(t -> {
                    t.setRevoked(true);
                    return jwtTokenRepository.save(t);
                })
                .then();

    }


    @Override
    public Mono<AuthResponse> handleGithubCallback(CallbackBody callbackBody) {
        return githubUserService.getOAuthService(webClient, this).handleProviderCallback(callbackBody, null);
    }

    @Override
    public Mono<AuthResponse> handleGoogleCallback(CallbackBody callbackBody, String codeVerifier) {
        return googleUserService.getOAuthService(webClient, this).handleProviderCallback(callbackBody, codeVerifier);
    }


}

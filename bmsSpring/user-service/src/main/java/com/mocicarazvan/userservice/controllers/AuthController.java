package com.mocicarazvan.userservice.controllers;

import com.mocicarazvan.userservice.dtos.auth.requests.CallbackBody;
import com.mocicarazvan.userservice.dtos.auth.requests.LoginRequest;
import com.mocicarazvan.userservice.dtos.auth.requests.RegisterRequest;
import com.mocicarazvan.userservice.dtos.auth.requests.TokenValidationRequest;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.dtos.auth.response.TokenValidationResponse;
import com.mocicarazvan.userservice.dtos.otp.OTPRequest;
import com.mocicarazvan.userservice.dtos.otp.ResetPasswordRequest;
import com.mocicarazvan.userservice.pkce.CodeVerifierResponse;
import com.mocicarazvan.userservice.pkce.PKCEGoogle;
import com.mocicarazvan.userservice.services.AuthService;
import com.mocicarazvan.userservice.services.OTPTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller")
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final OTPTokenService OTPTokenService;
    private final PKCEGoogle pkceGoogle;


    @PostMapping(value = "/register", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        return authService.register(registerRequest)
                .map(resp -> ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, createCookie(resp.getToken()).toString()).body(resp));
    }

    private ResponseCookie createCookie(String token) {
        return ResponseCookie.from("authToken", token)
                .httpOnly(true)
                .maxAge(Duration.ofDays(1))
                .path("/")
                .build();
    }

    @PostMapping(value = "/login", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        return authService.login(loginRequest)
                .map(resp -> ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, createCookie(resp.getToken()).toString()).body(resp));
    }

    @PostMapping(value = "/validateToken", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<TokenValidationResponse>> validateToken(
            @Valid @RequestBody TokenValidationRequest tokenValidationRequest
    ) {
        return authService.validateToken(tokenValidationRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/github/callback", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<AuthResponse>> githubCallback(
            @Valid @RequestBody CallbackBody callbackBody
    ) {
        log.error(callbackBody.toString());
        return authService.handleGithubCallback(callbackBody)
                .map(resp -> ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, createCookie(resp.getToken()).toString()).body(resp));
    }

    @PostMapping(value = "/google/callback", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<AuthResponse>> googleCallback(
            @Valid @RequestBody CallbackBody callbackBody) {
        log.error("Received callback: {}", callbackBody);

        return pkceGoogle.handleAuthResponse(
                callbackBody, authService::handleGoogleCallback
        ).flatMap(resp -> Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createCookie(resp.getToken()).toString())
                .body(resp)));

    }

    @GetMapping("/google/login")
    public Mono<ResponseEntity<CodeVerifierResponse>> googleLogin(@RequestParam String state) {
        return pkceGoogle.handleAuthorizationCode(state)
                .map(m -> ResponseEntity.status(200).header(HttpHeaders.LOCATION, m.getUrl()).body(m));

    }


    @PostMapping(value = "/resetPassword", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    public Mono<ResponseEntity<Void>> resetPassword(
            @Valid @RequestBody OTPRequest OTPRequest
    ) {
        return OTPTokenService.generatePasswordToken(OTPRequest)
                .then(Mono.just(ResponseEntity.ok().build()));

    }

    @PostMapping(value = "/changePassword", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    public Mono<ResponseEntity<AuthResponse>> changePassword(
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        return OTPTokenService.resetPassword(resetPasswordRequest)
                .map(ResponseEntity::ok);

    }

    @PostMapping(value = "/verifyEmail", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    public Mono<ResponseEntity<Void>> verifyEmail(@Valid @RequestBody OTPRequest otpRequest) {
        return OTPTokenService.generateEmailVerificationToken(otpRequest)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @PostMapping(value = "/confirmEmail", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    public Mono<ResponseEntity<Void>> confirmEmail(@RequestParam String email, @RequestParam String token) {
        return OTPTokenService.confirmEmail(email, token)
                .then(Mono.just(ResponseEntity.ok().build()));

    }

}
//"/auth/resetPassword","/auth/changePassword"
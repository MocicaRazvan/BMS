package com.mocicarazvan.userservice.services;


import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.dtos.otp.OTPRequest;
import com.mocicarazvan.userservice.dtos.otp.ResetPasswordRequest;
import reactor.core.publisher.Mono;

public interface OTPTokenService {
    Mono<Void> generatePasswordToken(OTPRequest OTPRequest);

    Mono<AuthResponse> resetPassword(ResetPasswordRequest resetPasswordRequest);

    Mono<Void> generateEmailVerificationToken(OTPRequest otpRequest);

    Mono<Void> confirmEmail(String email, String token);
}

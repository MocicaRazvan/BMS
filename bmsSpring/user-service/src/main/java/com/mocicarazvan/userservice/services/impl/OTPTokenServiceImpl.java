package com.mocicarazvan.userservice.services.impl;


import com.mocicarazvan.templatemodule.email.EmailUtils;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.exceptions.common.UsernameNotFoundException;
import com.mocicarazvan.userservice.dtos.auth.requests.LoginRequest;
import com.mocicarazvan.userservice.dtos.auth.response.AuthResponse;
import com.mocicarazvan.userservice.dtos.otp.OTPRequest;
import com.mocicarazvan.userservice.dtos.otp.ResetPasswordRequest;
import com.mocicarazvan.userservice.email.EmailTemplates;
import com.mocicarazvan.userservice.enums.OTPType;
import com.mocicarazvan.userservice.exceptions.EmailAlreadyVerified;
import com.mocicarazvan.userservice.models.OTPToken;
import com.mocicarazvan.userservice.models.UserCustom;
import com.mocicarazvan.userservice.repositories.OTPTokenRepository;
import com.mocicarazvan.userservice.repositories.UserRepository;
import com.mocicarazvan.userservice.services.AuthService;
import com.mocicarazvan.userservice.services.OTPTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OTPTokenServiceImpl implements OTPTokenService {

    private final long expireInSeconds = 60 * 60 * 24;

    private final UserRepository userRepository;
    private final OTPTokenRepository OTPTokenRepository;
    private final EmailUtils emailUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Value("${front.url}")
    private String frontUrl;

    public Mono<Void> generatePasswordToken(OTPRequest otpRequest) {
        return generateToken(otpRequest, OTPType.PASSWORD)
                .flatMap(tuple -> sendResetEmail(tuple.getT1().getEmail(), tuple.getT2().getToken()));
    }

    public Mono<Void> generateEmailVerificationToken(OTPRequest otpRequest) {
        return generateToken(otpRequest, OTPType.CONFIRM_EMAIL)
                .filter(t -> !t.getT1().isEmailVerified())
                .switchIfEmpty(Mono.error(new EmailAlreadyVerified(otpRequest.getEmail())))
                .flatMap(tuple -> sendEmailVerificationEmail(tuple.getT1().getEmail(), tuple.getT2().getToken(), tuple.getT1().getId()));
    }

    private Mono<Tuple2<UserCustom, OTPToken>> generateToken(OTPRequest otpRequest, OTPType type) {
        return userRepository.findByEmailAndProvider(otpRequest.getEmail(), AuthProvider.LOCAL)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with email: " + otpRequest.getEmail() + " not found")))
                .flatMap(user -> OTPTokenRepository.save(
                        OTPToken.builder()
                                .token(UUID.randomUUID().toString())
                                .expiresInSeconds(expireInSeconds)
                                .type(type)
                                .userId(user.getId()).build()
                ).flatMap(pt -> Mono.just(Tuples.of(user, pt))
                ));
    }

    private Mono<Void> sendResetEmail(String email, String token) {
//        String resetUrl = frontUrl + "/auth/reset-password?token=" + token + "&email=" + email;
//        String emailContent = "<p>Click the link below to reset your password:</p>" +
//                "<a href=\"" + resetUrl + "\">Reset Password</a>";
        String resetUrl = STR."\{frontUrl}/auth/reset-password?token=\{token}&email=\{email}";
        String emailContent = EmailTemplates.resetPassword(frontUrl, resetUrl);
        return emailUtils.sendEmail(email, "Password Reset", emailContent);
    }

    private Mono<Void> sendEmailVerificationEmail(String email, String token, Long userId) {
//        String confirmUrl = frontUrl + "/auth/confirm-email?token=" + token + "&email=" + email + "&userId=" + userId;
//        String emailContent = "<p>Click the link below to confirm your email:</p>" +
//                "<a href=\"" + confirmUrl + "\">Confirm Email</a>";
        String confirmUrl = STR."\{frontUrl}/auth/confirm-email?token=\{token}&email=\{email}&userId=\{userId}";
        String emailContent = EmailTemplates.verifyEmail(frontUrl, confirmUrl);
        return emailUtils.sendEmail(email, "Email Verification", emailContent);
    }

    public Mono<Void> confirmEmail(String email, String token) {
//        return userRepository.findByEmailAndProvider(email, AuthProvider.LOCAL)
//                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with email: " + email + " not found")))
//                .flatMap(userCustom -> OTPTokenRepository.findByUserIdAndTokenAndType(userCustom.getId(), token, OTPType.CONFIRM_EMAIL)
//                        .filter(prt -> prt.getToken().equals(token) &&
//                                prt.getCreatedAt().plusSeconds(prt.getExpiresInSeconds()).isAfter(LocalDateTime.now()))
//                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid token")))
//                        .flatMap(vt -> {
//                            userCustom.setEmailVerified(true);
//                            return userRepository.save(userCustom)
//                                    .then(OTPTokenRepository.deleteAllByUserIdAndType(userCustom.getId(), OTPType.CONFIRM_EMAIL));
//                        }));

        return handleToken(email, token, AuthProvider.LOCAL, OTPType.CONFIRM_EMAIL, tuple -> {
            tuple.getT1().setEmailVerified(true);
            return userRepository.save(tuple.getT1())
                    .then(OTPTokenRepository.deleteAllByUserIdAndType(tuple.getT1().getId(), OTPType.CONFIRM_EMAIL));

        });

    }


    public Mono<AuthResponse> resetPassword(ResetPasswordRequest resetPasswordRequest) {

//        return userRepository.findByEmailAndProvider(resetPasswordRequest.getEmail(), AuthProvider.LOCAL)
//                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with email: " + resetPasswordRequest.getEmail() + " not found")))
//                .flatMap(userCustom -> OTPTokenRepository.findByUserIdAndTokenAndType(userCustom.getId(), resetPasswordRequest.getToken(), OTPType.PASSWORD)
//                        .filter(prt -> prt.getToken().equals(resetPasswordRequest.getToken()) &&
//                                prt.getCreatedAt().plusSeconds(prt.getExpiresInSeconds()).isAfter(LocalDateTime.now()))
//                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid token")))
//                        .flatMap(vt -> {
//                            userCustom.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
//                            return userRepository.save(userCustom)
//                                    .then(OTPTokenRepository.deleteAllByUserIdAndType(userCustom.getId(), OTPType.PASSWORD));
//                        }));

        return handleToken(resetPasswordRequest.getEmail(), resetPasswordRequest.getToken(), AuthProvider.LOCAL, OTPType.PASSWORD, tuple -> {
            tuple.getT1().setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
            return userRepository.save(tuple.getT1())
//                    .then(OTPTokenRepository.deleteAllByUserIdAndType(tuple.getT1().getId(), OTPType.PASSWORD))
                    .flatMap(userCustom -> OTPTokenRepository.deleteAllByUserIdAndType(tuple.getT1().getId(), OTPType.PASSWORD)
                            .then(authService.login(
                                    LoginRequest.builder()
                                            .password(resetPasswordRequest.getNewPassword())
                                            .email(userCustom.getEmail())
                                            .build()
                            )));

        });

    }

    private <T> Mono<T> handleToken(String email, String token, AuthProvider provider, OTPType type, Function<Tuple2<UserCustom, OTPToken>, Mono<T>> callback) {
        return userRepository.findByEmailAndProvider(email, provider)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with email: " + email + " not found")))
                .flatMap(userCustom -> OTPTokenRepository.findByUserIdAndTokenAndType(userCustom.getId(), token, type)
                        .filter(prt -> prt.getToken().equals(token) &&
                                prt.getCreatedAt().plusSeconds(prt.getExpiresInSeconds()).isAfter(LocalDateTime.now())
                        )
                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid token")))
                        .map(t -> Tuples.of(userCustom, t))
                        .flatMap(callback)
                );


    }
}

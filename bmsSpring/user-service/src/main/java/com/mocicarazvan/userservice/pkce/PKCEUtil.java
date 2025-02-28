package com.mocicarazvan.userservice.pkce;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class PKCEUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final ThreadLocal<MessageDigest> MESSAGE_DIGEST_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to create message digest in PKCEUtil");
            throw new RuntimeException(e);
        }
    });

    public static String generateCodeVerifier() {
//        log.info("Generating code verifier");
        byte[] codeVerifier = new byte[32];
        SECURE_RANDOM.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    public static String generateCodeChallenge(String codeVerifier) {
//        log.info("Generating code challenge");
        byte[] hash = MESSAGE_DIGEST_THREAD_LOCAL.get().digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

    }
}

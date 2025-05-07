package com.mocicarazvan.websocketservice.utils;

import com.mocicarazvan.websocketservice.config.EncryptionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class AESUtils {

    private static final int TAG_LENGTH_BIT = 128;  // 128-bit auth tag
    private static final int IV_LENGTH = 12;   // 96 bits, recommended for GCM
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private final SecretKeySpec key;
    private final ThreadPoolTaskExecutor encryptionExecutor;
    private final ThreadLocal<Cipher> encryptCipher;
    private final ThreadLocal<Cipher> decryptCipher;
    private final ThreadLocal<SecureRandom> secureRandom = ThreadLocal.withInitial(SecureRandom::new);

    public AESUtils(@Qualifier("encryptionExecutor") ThreadPoolTaskExecutor encryptionExecutor,
                    EncryptionProperties encryptionProperties) {
        this.encryptionExecutor = encryptionExecutor;
        this.key = new SecretKeySpec(encryptionProperties.getSecretKeyBytes(), "AES");
        this.encryptCipher = ThreadLocal.withInitial(this::initCipherInstance);
        this.decryptCipher = ThreadLocal.withInitial(this::initCipherInstance);
    }


    public CompletableFuture<byte[]> encryptAsync(String plainText) {
        return CompletableFuture.supplyAsync(() -> encrypt(plainText), encryptionExecutor);
    }

    public CompletableFuture<String> decryptAsync(byte[] cipherBytes) {
        return CompletableFuture.supplyAsync(() -> decrypt(cipherBytes), encryptionExecutor);
    }

    private Cipher initCipherInstance() {
        try {
            return Cipher.getInstance(ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Cipher", e);
        }
    }

    private byte[] encrypt(String plainText) {
        if (plainText == null) {
            return new byte[0];
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.get().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

            Cipher cipher = encryptCipher.get();
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
            int outputSize = cipher.getOutputSize(plainBytes.length);
            ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH + outputSize);

            buffer.put(iv);
            cipher.doFinal(ByteBuffer.wrap(plainBytes), buffer);

            return buffer.array();
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    private String decrypt(byte[] cipherMessage) {
        if (cipherMessage == null || cipherMessage.length < 16) {
            return "";
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(cipherMessage);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

            byte[] cipherBytes = new byte[buffer.remaining()];
            buffer.get(cipherBytes);

            Cipher cipher = decryptCipher.get();
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}

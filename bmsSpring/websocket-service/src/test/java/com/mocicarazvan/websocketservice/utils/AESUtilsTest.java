package com.mocicarazvan.websocketservice.utils;

import com.mocicarazvan.websocketservice.config.EncryptionProperties;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AESUtilsTest {


    private final ThreadPoolTaskExecutor encryptionExecutor = new ThreadPoolTaskExecutor();


    private final EncryptionProperties encryptionProperties = new EncryptionProperties();

    private AESUtils aesUtils;

    @BeforeEach
    void setUp() {
        String rawKey = "1234567890123456";
        String b64Key = Base64.getEncoder()
                .encodeToString(rawKey.getBytes(StandardCharsets.UTF_8));
        encryptionProperties.setSecretKey(b64Key);

        encryptionExecutor.initialize();
        aesUtils = new AESUtils(encryptionExecutor, encryptionProperties);
    }


    @Test
    @DisplayName("encrypts plain text successfully")
    @SneakyThrows
    void encryptsPlainTextSuccessfully() {
        String plainText = "Hello, World!";
        byte[] encrypted = aesUtils.encryptAsync(plainText).get();
        assertNotNull(encrypted);
        assertTrue(encrypted.length >= 12 + 16);
    }


    @Test
    @DisplayName("decrypts encrypted text successfully")
    @SneakyThrows
    void decryptsEncryptedTextSuccessfully() {
        String plainText = "Hello, World!";
        byte[] encrypted = aesUtils.encryptAsync(plainText).get();
        String decrypted = aesUtils.decryptAsync(encrypted).get();
        assertEquals(plainText, decrypted);
    }


    @Test
    @DisplayName("return empty byte for null text")
    @SneakyThrows
    void returnEmptyByteForNullText() {
        byte[] encrypted = aesUtils.encryptAsync(null).get();
        assertEquals(0, encrypted.length);
    }

    @Test
    @DisplayName("return empty string for null byte")
    @SneakyThrows
    void returnEmptyStringForNullByte() {
        String decrypted = aesUtils.decryptAsync(null).get();
        assertEquals("", decrypted);
    }

}
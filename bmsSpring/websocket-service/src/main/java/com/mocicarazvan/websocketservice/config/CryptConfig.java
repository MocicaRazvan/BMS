package com.mocicarazvan.websocketservice.config;


import com.mocicarazvan.websocketservice.hibernate.ContentEncryptor;
import com.mocicarazvan.websocketservice.utils.AESUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CryptConfig {


    private final AESUtils aesUtils;

    @Bean
    public ContentEncryptor chatMessageEncryptor() {
        ContentEncryptor encryptor = new ContentEncryptor();
        ContentEncryptor.setAesUtils(aesUtils);
        log.info("ChatMessageEncryptor initialized");
        return encryptor;
    }
}

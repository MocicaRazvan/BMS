package com.mocicarazvan.gatewayservice.config;


import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class NextJSSecretKeyConfig {

    @Value("${next.auth.auth.secret}")
    private String secretKey;


    @Bean
    public DirectDecrypter directDecrypter() throws KeyLengthException {
        String context = "NextAuth.js Generated Encryption Key";
        byte[] secretBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(secretBytes, new byte[0], context.getBytes(StandardCharsets.UTF_8)));
        byte[] derivedKey = new byte[32];
        hkdf.generateBytes(derivedKey, 0, derivedKey.length);
        return new DirectDecrypter(new SecretKeySpec(derivedKey, "AES"));
    }


}

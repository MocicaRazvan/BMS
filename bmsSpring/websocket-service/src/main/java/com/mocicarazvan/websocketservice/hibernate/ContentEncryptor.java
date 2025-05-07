package com.mocicarazvan.websocketservice.hibernate;

import com.mocicarazvan.websocketservice.models.generic.EncryptedContent;
import com.mocicarazvan.websocketservice.utils.AESUtils;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Setter;


public class ContentEncryptor {

    @Setter
    private static AESUtils aesUtils;

    @PrePersist
    @PreUpdate
    public void encrypt(EncryptedContent msg) {
        checkAesUtil();
        if (msg.getContent() != null) {
            msg.setEncryptedContent(aesUtils.encryptAsync(msg.getContent()).join());
        }
    }

    @PostLoad
    public void decrypt(EncryptedContent msg) {
        checkAesUtil();
        if (msg.getEncryptedContent() != null) {
            msg.setContent(aesUtils.decryptAsync(msg.getEncryptedContent()).join());
        }
    }

    private void checkAesUtil() {
        if (aesUtils == null) {
            throw new IllegalStateException("AESUtil is not set. Please set it before using the encryptor.");
        }
    }
}
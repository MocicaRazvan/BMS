package com.mocicarazvan.websocketservice.models.generic;

import com.mocicarazvan.websocketservice.hibernate.ContentEncryptor;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
@EntityListeners(ContentEncryptor.class)
public abstract class EncryptedContent extends IdGenerated {
    @Transient
    private String content;

    @Column(nullable = false, name = "encrypted_content")
    private byte[] encryptedContent;
}

package com.mocicarazvan.userservice.models;


import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import com.mocicarazvan.userservice.enums.OTPType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("otp_token")
public class OTPToken extends ManyToOneUser {
    private String token;
    @Column("expires_in_seconds")
    private long expiresInSeconds;

    private OTPType type;

}

package com.mocicarazvan.userservice.models;

import com.mocicarazvan.templatemodule.models.IdGenerated;
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
@Table(name = "jwt_token")
public class JwtToken extends IdGenerated {

    @Column("token")
    private String token;

    @Column("revoked")
    private boolean revoked;

    @Column("user_id")
    private Long userId;
}

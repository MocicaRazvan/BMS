package com.mocicarazvan.userservice.models;

import com.mocicarazvan.templatemodule.models.IdGenerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "oauth_state")
public class OauthState extends IdGenerated implements Cloneable {

    private String state;
    private String codeVerifier;

    @Override
    public OauthState clone() {
        return (OauthState) super.clone();
    }
}

package com.mocicarazvan.userservice.models;


import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.models.IdGenerated;
import com.mocicarazvan.userservice.authorities.GrantedAuthority;
import com.mocicarazvan.userservice.authorities.SimpleGrantedAuthority;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Collection;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "user_custom")
public class UserCustom extends IdGenerated implements Cloneable {
    @Column("first_name")
    private String firstName;
    @Column("last_name")
    private String lastName;
    @Column("email")
    private String email;
    @Column("password")
    private String password;
    @Column("role")
    @Builder.Default
    private Role role = Role.ROLE_USER;
    @Column("image")
    private String image;

    @Column("provider")
    private AuthProvider provider;

    @Column("is_email_verified")
    private boolean emailVerified;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    public String getUsername() {
        return email;
    }

    @Override
    public UserCustom clone() {
        return (UserCustom) super.clone();
    }
}

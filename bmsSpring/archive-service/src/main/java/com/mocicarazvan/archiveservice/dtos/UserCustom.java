package com.mocicarazvan.archiveservice.dtos;


import com.mocicarazvan.archiveservice.dtos.enums.AuthProvider;
import com.mocicarazvan.archiveservice.dtos.enums.Role;
import com.mocicarazvan.archiveservice.dtos.generic.IdGenerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserCustom extends IdGenerated {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role = Role.ROLE_USER;
    private String image;
    private AuthProvider provider;
    private boolean emailVerified;

    @Override
    public String toString() {
        return "UserCustom{" + "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", image='" + image + '\'' +
                ", provider=" + provider +
                ", emailVerified=" + emailVerified +
                '}';
    }
}

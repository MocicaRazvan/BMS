package com.mocicarazvan.websocketservice.dtos.user.reactive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ReactiveUserDto {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String firstName;
    private String lastName;
    private String email;
    private Role role = Role.ROLE_USER;
    private AuthProvider provider;
    private String image;
    private boolean emailVerified;
}

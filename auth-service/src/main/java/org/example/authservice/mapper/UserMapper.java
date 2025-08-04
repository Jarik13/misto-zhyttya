package org.example.authservice.mapper;

import lombok.RequiredArgsConstructor;
import org.example.authservice.dto.auth.RegistrationRequest;
import org.example.authservice.model.Role;
import org.example.authservice.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User toUser(RegistrationRequest request) {
        return User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .enabled(true)
                .build();
    }
}

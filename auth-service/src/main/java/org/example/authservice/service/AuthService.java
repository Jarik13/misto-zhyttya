package org.example.authservice.service;

import org.example.authservice.dto.auth.LoginRequest;
import org.example.authservice.dto.auth.RegistrationRequest;

public interface AuthService {
    void register(RegistrationRequest registrationRequest);
    void login(LoginRequest loginRequest);
    void logout();
}

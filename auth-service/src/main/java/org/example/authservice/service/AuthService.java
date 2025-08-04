package org.example.authservice.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.authservice.dto.auth.LoginRequest;
import org.example.authservice.dto.auth.RegistrationRequest;

public interface AuthService {
    void register(RegistrationRequest request, HttpServletResponse response);
    void login(LoginRequest request, HttpServletResponse response);
    void refreshAccessToken();
    void logout();
}

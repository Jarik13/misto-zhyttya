package org.example.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.authservice.dto.auth.LoginRequest;
import org.example.authservice.dto.auth.RegistrationRequest;

import java.util.UUID;

public interface AuthService {
    void register(RegistrationRequest request, HttpServletResponse response);
    void login(LoginRequest request, HttpServletResponse response);
    void refreshAccessToken();
    void logout(HttpServletRequest request, HttpServletResponse response);
}

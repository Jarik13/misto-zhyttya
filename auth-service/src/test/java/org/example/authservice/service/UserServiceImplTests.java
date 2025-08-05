package org.example.authservice.service;

import org.example.authservice.model.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTests {
    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void givenExistingUserEmail_whenLoadUserByUsername_thenReturnsUserDetails() {
        // given
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("securePassword");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // when
        UserDetails result = userService.loadUserByUsername(email);

        // then
        assertNotNull(result);
        assertEquals(email, result.getUsername());
        assertEquals("securePassword", result.getPassword());
    }

    @Test
    void givenNonExistingUserEmail_whenLoadUserByUsername_thenThrowsUsernameNotFoundException() {
        // given
        String email = "notfound@example.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        // when / then
        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(email)
        );

        assertEquals("User notfound@example.com not found", ex.getMessage());
    }

    @Test
    void givenNullEmail_whenLoadUserByUsername_thenThrowsException() {
        // given
        String email = null;

        // when / then
        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(email)
        );

        assertEquals("User null not found", ex.getMessage());
    }

    @Test
    void givenEmptyEmail_whenLoadUserByUsername_thenThrowsException() {
        // given
        String email = "";

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        // when / then
        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(email)
        );

        assertEquals("User  not found", ex.getMessage());
    }
}

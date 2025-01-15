package com.example.ShortenerProject.user;

import com.example.ShortenerProject.security.JwtUtil;
import com.example.ShortenerProject.user.dto.request.LoginRequest;
import com.example.ShortenerProject.user.dto.request.UserCreateRequest;
import com.example.ShortenerProject.user.dto.response.AuthResponse;
import com.example.ShortenerProject.user.dto.response.RegistrationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final UserService userService = mock(UserService.class);

    private final AuthController authController = new AuthController(authenticationManager, userDetailsService, jwtUtil, userService);

    @Test
    void testLoginSuccess() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("fake-jwt-token");

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof AuthResponse);
        assertEquals("fake-jwt-token", ((AuthResponse) response.getBody()).token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void testLoginBadCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        doThrow(new BadCredentialsException("Bad credentials")).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act
        ResponseEntity<?> response = authController.login(loginRequest);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid username or password", response.getBody());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userDetailsService, jwtUtil);
    }

    @Test
    void testRegistrationSuccess() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest("newuser", "password");
        UserDetails userDetails = mock(UserDetails.class);

        when(userService.createUser(request)).thenReturn("User created successfully");
        when(userDetailsService.loadUserByUsername("newuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("fake-jwt-token");

        // Act
        ResponseEntity<?> response = authController.registration(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof RegistrationResponse);
        RegistrationResponse registrationResponse = (RegistrationResponse) response.getBody();
        assertEquals("fake-jwt-token", registrationResponse.token());
        assertEquals("newuser", registrationResponse.userResponse().username());
        assertEquals("User created successfully", registrationResponse.message());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).createUser(request);
        verify(userDetailsService).loadUserByUsername("newuser");
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void testRegistrationFailure() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest("newuser", "password");
        when(userService.createUser(request)).thenThrow(new IllegalArgumentException("Username already exists"));

        // Act
        ResponseEntity<?> response = authController.registration(request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username already exists", response.getBody());
        verify(userService).createUser(request);
        verifyNoInteractions(authenticationManager, userDetailsService, jwtUtil);
    }

    @Test
    void testRegistrationInternalServerError() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest("newuser", "password");
        when(userService.createUser(request)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<?> response = authController.registration(request);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Internal Server Error", response.getBody());
        verify(userService).createUser(request);
        verifyNoInteractions(authenticationManager, userDetailsService, jwtUtil);
    }
}

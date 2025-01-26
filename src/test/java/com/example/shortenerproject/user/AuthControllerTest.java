package com.example.shortenerproject.user;

import com.example.shortenerproject.exception.dto.ErrorResponse;
import com.example.shortenerproject.security.JwtUtil;
import com.example.shortenerproject.user.dto.request.LoginRequest;
import com.example.shortenerproject.user.dto.request.UserCreateRequest;
import com.example.shortenerproject.user.dto.response.AuthResponse;
import com.example.shortenerproject.user.dto.response.RegistrationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthControllerTest {

    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final UserService userService = mock(UserService.class);

    private final AuthController authController = new AuthController(authenticationManager, userDetailsService, jwtUtil, userService);

    @Test
    void testLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails, 1L)).thenReturn("fake-jwt-token");

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof AuthResponse);
        assertEquals("fake-jwt-token", ((AuthResponse) response.getBody()).token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtUtil).generateToken(userDetails, 1L);
    }

    @Test
    void testLoginBadCredentials() {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        Exception exception = assertThrows(BadCredentialsException.class, () -> authController.login(loginRequest));

        assertEquals("Bad credentials", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userDetailsService, jwtUtil);
    }

    @Test
    void testRegistrationSuccess() {
        UserCreateRequest request = new UserCreateRequest("newuser", "Password1");
        UserDetails userDetails = mock(UserDetails.class);
        User mockedUser = mock(User.class);

        when(mockedUser.getId()).thenReturn(1L);
        when(mockedUser.getUsername()).thenReturn("newuser");

        when(userService.createUser(request)).thenReturn("User created successfully");
        when(userService.getUserByUsername("newuser")).thenReturn(mockedUser);
        when(userDetailsService.loadUserByUsername("newuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails, 1L)).thenReturn("fake-jwt-token");

        ResponseEntity<?> response = authController.registration(request);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof RegistrationResponse);
        RegistrationResponse registrationResponse = (RegistrationResponse) response.getBody();
        assertEquals("fake-jwt-token", registrationResponse.token());
        assertEquals("newuser", registrationResponse.userResponse().username());
        assertEquals("User created successfully", registrationResponse.message());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).createUser(request);
        verify(userService).getUserByUsername("newuser");
        verify(userDetailsService).loadUserByUsername("newuser");
        verify(jwtUtil).generateToken(userDetails, 1L);
    }


    @Test
    void testRegistrationUsernameExists() {
        UserCreateRequest request = new UserCreateRequest("existinguser", "Password1");
        when(userService.existsByUsername("existinguser")).thenReturn(true);

        ResponseEntity<?> response = authController.registration(request);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("400", errorResponse.status());
        assertEquals("Username already exists. Please try again.", errorResponse.message());
        verify(userService).existsByUsername("existinguser");
        verifyNoInteractions(authenticationManager, userDetailsService, jwtUtil);
    }

    @Test
    void testRegistrationPasswordInvalid() {
        UserCreateRequest request = new UserCreateRequest("newuser", "short");
        when(userService.existsByUsername("newuser")).thenReturn(false);

        ResponseEntity<?> response = authController.registration(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Password must contain at least 8 characters, including digits, uppercase and lowercase letters.", response.getBody());
        verifyNoInteractions(authenticationManager, userDetailsService, jwtUtil);
    }

    @Test
    void testRegistrationInternalServerError() {
        UserCreateRequest request = new UserCreateRequest("newuser", "Password1");
        when(userService.createUser(request)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = authController.registration(request);

        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("INTERNAL_SERVER_ERROR", errorResponse.status());
        assertEquals("Internal Server Error", errorResponse.message());
        verify(userService).createUser(request);
        verifyNoInteractions(authenticationManager, userDetailsService, jwtUtil);
    }
}

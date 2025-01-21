package com.example.shortenerproject.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.mockito.Mockito.*;

class JwtRequestFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void doFilterInternal_ValidToken_AuthenticationSet() throws ServletException, IOException {

        String jwtToken = "valid.jwt.token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(jwtUtil.extractUsername(jwtToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(jwtToken, userDetails)).thenReturn(true);

        // Method call
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Verifying that authentication has been established
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        assert authentication.getPrincipal().equals(userDetails);

        // Checking that the filter chain has been called
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_NoAuthenticationSet() throws ServletException, IOException {

        String jwtToken = "invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(jwtUtil.extractUsername(jwtToken)).thenThrow(new IllegalArgumentException("Unable to parse token"));

        // Method call
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Verify that authentication has not been established
        assert SecurityContextHolder.getContext().getAuthentication() == null;

        // Checking that the filter chain has been called
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExpiredToken_NoAuthenticationSet() throws ServletException, IOException {
        // Clearing the SecurityContextHolder before the test
        SecurityContextHolder.clearContext();


        String jwtToken = "expired.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(jwtUtil.extractUsername(jwtToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // Method call
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Verify that authentication has not been established
        assert SecurityContextHolder.getContext().getAuthentication() == null;

        // Checking that the filter chain is called
        verify(filterChain, times(1)).doFilter(request, response);

        // Checking that the logger warned about an expired token
        verify(jwtUtil, times(0)).validateToken(anyString(), any(UserDetails.class));
    }

}

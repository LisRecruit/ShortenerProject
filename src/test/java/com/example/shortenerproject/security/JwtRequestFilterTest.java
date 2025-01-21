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
        // Мокування даних
        String jwtToken = "valid.jwt.token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(jwtUtil.extractUsername(jwtToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(jwtToken, userDetails)).thenReturn(true);

        // Виклик методу
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Перевірка, що автентифікація була встановлена
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        assert authentication.getPrincipal().equals(userDetails);

        // Перевірка, що ланцюжок фільтрів було викликано
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_NoAuthenticationSet() throws ServletException, IOException {
        // Мокування даних
        String jwtToken = "invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(jwtUtil.extractUsername(jwtToken)).thenThrow(new IllegalArgumentException("Unable to parse token"));

        // Виклик методу
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Перевірка, що автентифікація не була встановлена
        assert SecurityContextHolder.getContext().getAuthentication() == null;

        // Перевірка, що ланцюжок фільтрів було викликано
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExpiredToken_NoAuthenticationSet() throws ServletException, IOException {
        // Очистка SecurityContextHolder перед тестом
        SecurityContextHolder.clearContext();

        // Мокування даних
        String jwtToken = "expired.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(jwtUtil.extractUsername(jwtToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // Виклик методу
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Перевірка, що автентифікація не була встановлена
        assert SecurityContextHolder.getContext().getAuthentication() == null;

        // Перевірка, що ланцюжок фільтрів викликано
        verify(filterChain, times(1)).doFilter(request, response);

        // Перевірка, що логер попередив про прострочений токен
        verify(jwtUtil, times(0)).validateToken(anyString(), any(UserDetails.class));
    }

}

package com.example.shortenerproject.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=YourSuperSecretKeyAtLeast32BytesLong",
        "jwt.expiration=3600000" // 1 година в мілісекундах
})
 class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetails userDetails;



    @BeforeEach
    public void setUp() {
        Mockito.when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void testGenerateToken() {
        Long userId = 1L;
        String token = jwtUtil.generateToken(userDetails, userId);
        assertNotNull(token);
    }

    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken(userDetails, 1L);
        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testExtractExpiration() {
        String token = jwtUtil.generateToken(userDetails, 1L);
        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testValidateToken() {
        String token = jwtUtil.generateToken(userDetails, 1L);
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testIsTokenExpired() {
        String token = jwtUtil.generateToken(userDetails, 1L);
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testExtractClaim() {
        String token = jwtUtil.generateToken(userDetails, 1L);
        String subject = jwtUtil.extractClaim(token, Claims::getSubject);
        assertEquals("testuser", subject);
    }
}

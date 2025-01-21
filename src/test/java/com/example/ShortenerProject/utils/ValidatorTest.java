package com.example.ShortenerProject.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidatorTest {

    private RestTemplate restTemplate;
    private Validator validator;

    @BeforeEach
    void setUp() {
        // Создаем мок для RestTemplate
        restTemplate = mock(RestTemplate.class);
        validator = new Validator(restTemplate);
    }

    @Test
    void testIsValidPassword_ValidPassword() {
        String validPassword = "Valid1234";
        assertTrue(Validator.isValidPassword(validPassword), "Valid password should return true");
    }

    @Test
    void testIsValidPassword_InvalidPassword() {
        String invalidPassword = "invalid";
        assertFalse(Validator.isValidPassword(invalidPassword), "Invalid password should return false");
    }

    @Test
    void testIsValidPassword_NullPassword() {
        assertFalse(Validator.isValidPassword(null), "Null password should return false");
    }

    @Test
    void testIsValidUrl_ValidUrl() {
        String validUrl = "https://www.google.com";

        // Мокирование RestTemplate для успешного ответа
        ResponseEntity<Void> validResponse = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.getForEntity(validUrl, Void.class)).thenReturn(validResponse);

        assertTrue(validator.isValidUrl(validUrl), "Valid URL should return true");
    }

    @Test
    void testIsValidUrl_InvalidUrl() {
        String invalidUrl = "invalid-url";

        // Мокирование RestTemplate для ответа с ошибкой
        ResponseEntity<Void> invalidResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(invalidUrl, Void.class)).thenReturn(invalidResponse);

        assertFalse(validator.isValidUrl(invalidUrl), "Invalid URL should return false");
    }

    @Test
    void testIsValidUrl_UrlThrowsException() {
        String invalidUrl = "https://invalid-url.com";

        // Мокирование исключения (например, при недоступности сервера)
        when(restTemplate.getForEntity(invalidUrl, Void.class)).thenThrow(new RuntimeException("Network error"));

        assertFalse(validator.isValidUrl(invalidUrl), "URL with exception should return false");
    }
}

package com.example.ShortenerProject.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class BadCredentialsExceptionTest {
    @Test
    void testBadCredentialsExceptionWithMessage() {

        String expectedMessage = "Invalid credentials";
        BadCredentialsException exception = new BadCredentialsException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
    }
}

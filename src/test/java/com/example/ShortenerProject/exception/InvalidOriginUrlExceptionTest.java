package com.example.ShortenerProject.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidOriginUrlExceptionTest {
    @Test
    void testInvalidOriginUrlException() {
        String expectedMessage = "Origin URL is invalid";
        InvalidOriginUrlException exception = new InvalidOriginUrlException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
    }
}

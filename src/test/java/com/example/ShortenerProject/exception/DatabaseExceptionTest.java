package com.example.ShortenerProject.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseExceptionTest {
    @Test
    void testDatabaseExceptionWithMessage() {

        String expectedMessage = "Database connection failed";
        DatabaseException exception = new DatabaseException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
    }
}

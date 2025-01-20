package com.example.ShortenerProject.exception;

import com.example.ShortenerProject.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityNotFoundExceptionTest {
    @Test
    void testEntityNotFoundExceptionWithMessage() {
        String expectedMessage = "User with id = 123 does not exist.";
        Class<?> entityClass = User.class;
        String paramName = "id";
        Object paramValue = 123;

        EntityNotFoundException exception = new EntityNotFoundException(entityClass, paramName, paramValue);

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testEntityNotFoundExceptionWithCustomMessage() {
        String expectedMessage = "Entity not found";

        EntityNotFoundException exception = new EntityNotFoundException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
    }
}

package com.example.ShortenerProject.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CantBeNullExceptionTest {
     @Test
     void testCantBeNullExceptionWithMessage() {
         String expectedMessage = "Custom message";

         CantBeNullException exception = new CantBeNullException(expectedMessage);

         assertEquals(expectedMessage, exception.getMessage());
     }

     @Test
     void testCantBeNullExceptionWithEntityClass() {
         Class<?> entityClass = String.class;

         String expectedMessage = entityClass.getSimpleName() + " can not be null";

         CantBeNullException exception = new CantBeNullException(entityClass);

         assertEquals(expectedMessage, exception.getMessage());
     }
}

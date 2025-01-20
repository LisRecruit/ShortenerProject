package com.example.ShortenerProject.exception;

public class CantBeNullException extends RuntimeException{
    public CantBeNullException(String message) {
        super(message);
    }

    public CantBeNullException(Class<?> entityClass) {
        super(generateMessage(entityClass));
    }


    public static String generateMessage(Class<?> entityClass) {

            return entityClass.getSimpleName() + " can not be null";

    }
}

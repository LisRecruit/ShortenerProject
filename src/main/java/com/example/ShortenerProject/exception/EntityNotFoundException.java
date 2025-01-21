package com.example.ShortenerProject.exception;

public class EntityNotFoundException extends DatabaseException{
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(Class<?> entityClass, String paramName, Object paramValue) {
        super(generateMessage(entityClass, paramName, paramValue));
    }

    private static String generateMessage(Class<?> entityClass, String paramName, Object paramValue) {
        return entityClass.getSimpleName() + " with " + paramName + " = " + paramValue + " does not exist.";
    }
}

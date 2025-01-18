package com.example.ShortenerProject.exception;

public class EntityNotFoundException extends DatabaseException{
    public EntityNotFoundException(String message) {
        super(message);
    }
}

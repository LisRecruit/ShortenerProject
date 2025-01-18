package com.example.ShortenerProject.exception;

public class InvalidOriginUrlException extends RuntimeException{
    public InvalidOriginUrlException(String message) {
        super(message);
    }
}

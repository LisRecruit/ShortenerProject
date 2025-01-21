package com.example.shortenerproject.exception;

public class InvalidOriginUrlException extends RuntimeException{
    public InvalidOriginUrlException(String message) {
        super(message);
    }
}

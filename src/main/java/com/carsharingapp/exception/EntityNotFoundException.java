package com.carsharingapp.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String massage) {
        super(massage);
    }
}

package com.carsharingapp.exception;

public class RentalIsNotActiveException extends RuntimeException {
    public RentalIsNotActiveException(String massage) {
        super(massage);
    }
}

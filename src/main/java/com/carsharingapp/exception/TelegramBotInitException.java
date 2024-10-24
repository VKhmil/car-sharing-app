package com.carsharingapp.exception;

public class TelegramBotInitException extends RuntimeException {
    public TelegramBotInitException(String message, Throwable cause) {
        super(message, cause);
    }
}

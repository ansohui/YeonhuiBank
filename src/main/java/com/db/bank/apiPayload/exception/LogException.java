package com.db.bank.apiPayload.exception;

public class LogException extends RuntimeException{
    public LogException(String message) {
        super(message);
    }

    public static class InvalidLogArgumentException extends LogException{
        public InvalidLogArgumentException(String message) {
            super(message);
        }
    }
}

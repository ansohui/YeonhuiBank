package com.db.bank.apiPayload.exception;

public class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }

    public static class UserNonExistsException extends AccountException{
        public UserNonExistsException(String message) {
            super(message);
        }
    }


}


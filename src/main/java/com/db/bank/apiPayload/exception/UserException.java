package com.db.bank.apiPayload.exception;

public class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }

    public static class UserNonExistsException extends UserException{
        public UserNonExistsException(String message) {
            super(message);
        }
    }
    public static class UserAlreadyExistsException extends UserException{
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class InvalidLoginException extends UserException{
        public InvalidLoginException(String message) { super(message); }
    }


}


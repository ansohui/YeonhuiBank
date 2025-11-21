package com.db.bank.apiPayload.exception;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccountExceptionHandler {
    @ExceptionHandler(AccountException.AccountNonExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountException(AccountException.AccountNonExistsException ex) {
        return new ResponseEntity<>(ApiResponse.onFailure(Status.ACCOUNT_NON_PRESENT), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(AccountException.AccountAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountException(AccountException.AccountAlreadyExistsException ex){
        return new ResponseEntity<>(ApiResponse.onFailure(Status.ACCOUNT_ALREADY_PRESENT), HttpStatus.CONFLICT);
    }
    @ExceptionHandler(AccountException.UnauthorizedAccountAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountException(AccountException.UnauthorizedAccountAccessException ex){
        return new ResponseEntity<>(ApiResponse.onFailure(Status.UNAUTHORIZED_ACCOUNT), HttpStatus.FORBIDDEN);
    }


}

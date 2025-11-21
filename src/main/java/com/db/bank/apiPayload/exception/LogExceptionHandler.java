package com.db.bank.apiPayload.exception;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LogExceptionHandler {
    @ExceptionHandler(LogException.InvalidLogArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidLogArgument(LogException.InvalidLogArgumentException ex) {
        return new ResponseEntity<>(
                ApiResponse.onFailure(Status.INVALID_LOG_ARGUMENT),
                HttpStatus.BAD_REQUEST
        );
    }

}

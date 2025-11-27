package com.db.bank.apiPayload.exception;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TransactionExceptionHandler {

    @ExceptionHandler(TransactionException.TransactionNonExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleTransactionNonExists(TransactionException.TransactionNonExistsException ex) {
        return new ResponseEntity<>(ApiResponse.onFailure(Status.TRANSACTION_NON_PRESENT), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(TransactionException.InsufficientFundsException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficientFundsException(TransactionException.InsufficientFundsException ex) {
        return new ResponseEntity<>(ApiResponse.onFailure(Status.TRANSACTION_NON_PRESENT), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(TransactionException.AccountLockedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountLockedException(TransactionException.AccountLockedException ex) {
        return new ResponseEntity<>(ApiResponse.onFailure(Status.TRANSACTION_NON_PRESENT), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(TransactionException.DailyLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleDailyLimitExceededException(TransactionException.DailyLimitExceededException ex) {
        return new ResponseEntity<>(ApiResponse.onFailure(Status.TRANSACTION_NON_PRESENT), HttpStatus.NOT_FOUND);
    }

}

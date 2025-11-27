package com.db.bank.apiPayload.exception;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TransferFailureReasonExceptionHandler {
    //사유코드 중복 예외
    @ExceptionHandler(TransferFailureReasonException.DuplicateReasonCodeException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateReasonCodeException(TransferFailureReasonException.DuplicateReasonCodeException e) {
        return new ResponseEntity<>(ApiResponse.onFailure(Status.REASON_CODE_DUPLICATE), HttpStatus.CONFLICT);
    }
    @ExceptionHandler(TransferFailureReasonException.ReasonCodeNonExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleNonExistsReasonCodeException(TransferFailureReasonException.ReasonCodeNonExistsException e) {
        return new ResponseEntity<>(ApiResponse.onFailure(Status.REASON_CODE_NON_PRESENT), HttpStatus.NOT_FOUND);
    }
}

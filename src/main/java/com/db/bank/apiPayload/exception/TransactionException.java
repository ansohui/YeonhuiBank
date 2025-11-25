package com.db.bank.apiPayload.exception;

public class TransactionException extends RuntimeException {
  public TransactionException(String message) {
    super(message);
  }

  public static class TransactionNonExistsException extends TransactionException {
    public TransactionNonExistsException(String message) {
      super(message);
    }
  }
}
